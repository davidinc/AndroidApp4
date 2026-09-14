package com.dawit.androidapp4

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.dawit.androidapp4.databinding.ActivityMainBinding
import com.dawit.androidapp4.domain.SearchMode
import com.dawit.androidapp4.ui.detail.PodcastDetailActivity
import com.dawit.androidapp4.ui.main.PodcastAdapter
import com.dawit.androidapp4.ui.main.PodcastUiState
import com.dawit.androidapp4.ui.main.PodcastViewModel
import com.dawit.androidapp4.ui.main.PodcastViewModelFactory
import kotlinx.coroutines.launch

/**
 * Main entry screen of SuperPodcast.
 *
 * Android constructs Activity classes, so we do not declare our own
 * MainActivity constructor.
 *
 * MainActivity is responsible for:
 * 1. Displaying activity_main.xml.
 * 2. Connecting PodcastAdapter to RecyclerView.
 * 3. Reading search input.
 * 4. Sending user actions to PodcastViewModel.
 * 5. Displaying the ViewModel's state.
 */
class MainActivity : AppCompatActivity() {

    /**
     * View Binding generates ActivityMainBinding from activity_main.xml.
     *
     * lateinit means this property will be initialized in onCreate()
     * before it is used.
     */
    private lateinit var binding: ActivityMainBinding

    /**
     * The viewModels delegate asks our factory to construct the ViewModel.
     *
     * The ViewModel is created only when it is first needed.
     */
    private val viewModel: PodcastViewModel by viewModels {
        PodcastViewModelFactory(
            context = applicationContext
        )
    }

    /**
     * Android calls onCreate() when this screen is first created.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Convert activity_main.xml into usable View objects.
        binding = ActivityMainBinding.inflate(layoutInflater)

        // Display the root view of activity_main.xml.
        setContentView(binding.root)

        // Prepare the podcast result list.
        configurePodcastList()

        // Prepare the advanced-filter Spinner.
        configureFilterSpinner()

        // Connect both buttons to their actions.
        configureButtons()

        // Observe and display changes from PodcastViewModel.
        observeViewModel()
    }

    /**
     * Creates PodcastAdapter and connects it to RecyclerView.
     */
    private fun configurePodcastList() {

        /**
         * This lambda runs when the user selects a podcast row.
         *
         * We temporarily display a Toast. Later, this code will open
         * PodcastDetailActivity.
         */
        val podcastAdapter = PodcastAdapter { selectedPodcast ->
            val intent = PodcastDetailActivity.newIntent(
                context = this,
                podcast = selectedPodcast
            )
            startActivity(intent)
        }

        binding.podcastList.apply {

            /**
             * LinearLayoutManager displays items vertically,
             * one after another.
             */
            layoutManager = LinearLayoutManager(
                this@MainActivity
            )

            // Connect RecyclerView to PodcastAdapter.
            adapter = podcastAdapter

            /**
             * The row size is stable even though its content changes.
             * This can improve RecyclerView performance.
             */
            setHasFixedSize(true)
        }

        /**
         * Store the adapter in the RecyclerView.
         *
         * We retrieve it later when rendering the ViewModel state.
         */
    }

    /**
     * Adds all SearchMode labels to the Spinner.
     */
    private fun configureFilterSpinner() {

        // Extract the visible labels from the SearchMode enum.
        val filterNames = SearchMode.entries.map { searchMode ->
            searchMode.label
        }

        /**
         * ArrayAdapter connects the list of names to the Spinner.
         */
        val spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            filterNames
        )

        // Use Android's standard expanded dropdown appearance.
        spinnerAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        binding.filterSpinner.adapter = spinnerAdapter

        /**
         * React when the user selects a different filter.
         */
        binding.filterSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    // Find the SearchMode at the selected position.
                    val selectedMode = SearchMode.entries[position]

                    /**
                     * Only regex and minimum-word modes need an
                     * additional criteria value.
                     */
                    val requiresCriteria =
                        selectedMode == SearchMode.REGEX_TITLE ||
                                selectedMode == SearchMode.MINIMUM_WORDS

                    binding.criteriaInputLayout.isVisible =
                        requiresCriteria

                    // Change the input instruction for the selected mode.
                    binding.criteriaInputLayout.hint =
                        when (selectedMode) {
                            SearchMode.REGEX_TITLE ->
                                getString(R.string.regex_hint)

                            SearchMode.MINIMUM_WORDS ->
                                getString(R.string.minimum_words_hint)

                            else ->
                                getString(R.string.optional_criteria)
                        }
                }

                override fun onNothingSelected(
                    parent: AdapterView<*>?
                ) {
                    // No action is required.
                }
            }
    }

    /**
     * Connects the Search and Subscriptions buttons.
     */
    private fun configureButtons() {

        binding.searchButton.setOnClickListener {

            // Hide the keyboard before displaying results.
            hideKeyboard()

            // Send the user's input to PodcastViewModel.
            viewModel.search(
                query = binding.searchInput.text
                    ?.toString()
                    .orEmpty(),

                mode = selectedSearchMode(),

                criteria = binding.criteriaInput.text
                    ?.toString()
                    .orEmpty()
            )
        }

        binding.subscriptionsButton.setOnClickListener {
            viewModel.showSubscriptions()
        }
    }

    /**
     * Returns the SearchMode currently selected in the Spinner.
     */
    private fun selectedSearchMode(): SearchMode {
        val selectedPosition =
            binding.filterSpinner.selectedItemPosition

        return SearchMode.entries[selectedPosition]
    }

    /**
     * Collects the ViewModel's StateFlow.
     *
     * repeatOnLifecycle starts collecting when the Activity is visible
     * and stops collecting when it is no longer visible.
     */
    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    displayState(state)
                }
            }
        }
    }

    /**
     * Updates activity_main.xml using the latest UI state.
     */
    private fun displayState(state: PodcastUiState) {

        // Show or hide the progress indicator.
        binding.loadingIndicator.isVisible =
            state.isLoading

        // Display the instruction, result count or error message.
        binding.statusText.text =
            state.message

        /**
         * Retrieve the adapter attached to RecyclerView.
         *
         * submitList asks ListAdapter and DiffUtil to calculate
         * which rows need to be updated.
         */
        val podcastAdapter =
            binding.podcastList.adapter as PodcastAdapter

        podcastAdapter.submitList(state.podcasts)
    }

    /**
     * Hides the software keyboard after Search is selected.
     */
    private fun hideKeyboard() {
        val keyboardManager = getSystemService(
            Context.INPUT_METHOD_SERVICE
        ) as InputMethodManager

        keyboardManager.hideSoftInputFromWindow(
            binding.searchInput.windowToken,
            0
        )

        binding.searchInput.clearFocus()
    }
}