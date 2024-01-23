package xyz.arifz.materialspinner

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.ColorStateList
import android.graphics.Color
import android.text.Editable
import android.text.InputType
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.Filterable
import android.widget.ListAdapter
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.material.textfield.TextInputLayout
import xyz.arifz.materialspinner.ExtensionFunctions.dpToPx

class MaterialSpinner : TextInputLayout {

    private lateinit var autoCompleteTextView: AppCompatAutoCompleteTextView
    private var hintForColor = ""
    private var isRequired = false
    private var isSearchable = false
    private var items = ArrayList<String>()

    private var itemsList = listOf<SelectedItem>()
    private var currentSelectedItem: SelectedItem? = null

    private var searchTitle: String? = null
    var onSearchSpinnerItemClickListener: OnSearchSpinnerItemClickListener? = null
    var onSelectedItemClickListener: OnSelectedItemClickListener? = null

    init {
        setupTheme()
    }

    constructor(context: Context) : super(context) {
        init(context, null, R.style.TextInputLayoutStyle)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(
        context,
        attrs,
        R.style.TextInputLayoutStyle
    ) {
        init(context, attrs, R.style.TextInputLayoutStyle)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(context, attrs, R.style.TextInputLayoutStyle)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyle: Int) {
        setupView(context)
        setupAttributes(context, attrs)
        initWatchers()
        initListeners()
    }

    private fun setupTheme() {
        boxBackgroundColor = ContextCompat.getColor(context, R.color.color_white)
        boxBackgroundMode = BOX_BACKGROUND_OUTLINE
        boxStrokeWidth = 3
        boxStrokeWidthFocused = 3
        boxStrokeColor = ContextCompat.getColor(context, R.color.color_blue_crayola)
        setBoxStrokeColorStateList(
            ContextCompat.getColorStateList(
                context,
                R.color.colorset_box_stroke
            )!!
        )
        setHintTextAppearance(R.style.TextInputLayoutHintTextStyle)
    }

    private fun setupView(context: Context) {
        autoCompleteTextView = AppCompatAutoCompleteTextView(context)

        autoCompleteTextView.setCompoundDrawablesWithIntrinsicBounds(
            0,
            0,
            R.drawable.ic_dropdown_arrow,
            0
        )
        autoCompleteTextView.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 50.dpToPx())
        autoCompleteTextView.background = null
        autoCompleteTextView.setLines(1)
        autoCompleteTextView.maxLines = 1
        autoCompleteTextView.isSingleLine = true
        autoCompleteTextView.isClickable = false
        autoCompleteTextView.isFocusable = false
        autoCompleteTextView.isFocusableInTouchMode = false
        autoCompleteTextView.isCursorVisible = false
        autoCompleteTextView.inputType = InputType.TYPE_NULL
        addView(autoCompleteTextView)
        autoCompleteTextView.setPadding(20, 20, 20, 20)
        autoCompleteTextView.setOnClickListener { clickHandling() }
    }

    private fun setupAttributes(context: Context, attrs: AttributeSet?) {
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.MaterialSpinner)
            try {
                var hint = a.getString(R.styleable.MaterialSpinner_hint)
                isRequired = a.getBoolean(R.styleable.MaterialSpinner_isRequired, false)
                isSearchable = a.getBoolean(R.styleable.MaterialSpinner_isSearchable, false)
                if (isRequired) {
                    if (hint.isNullOrEmpty())
                        hint = ""

                    if (!hint.contains("*"))
                        hint += " *"
                    hintForColor = hint
                    setHintAsteriskColor(Color.RED)
                } else {
                    hintForColor = hint ?: ""
                    setHint(hint)
                }

                val isReadOnly = a.getBoolean(R.styleable.MaterialSpinner_isReadOnly, false)
                setReadOnly(isReadOnly)

                val radius = a.getFloat(R.styleable.MaterialSpinner_radius, 5f)
                setBoxCornerRadii(radius, radius, radius, radius)
                val fontSize = a.getFloat(R.styleable.MaterialSpinner_fontSize, 0f)
                setFontSize(fontSize)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            a.recycle()
        }
    }

    fun setReadOnly(state: Boolean) {
        if (state) {
            isClickable = false
            isFocusable = false
            isEnabled = false
            isFocusableInTouchMode = false
            setReadOnlyColor()
        }
    }

    private fun setReadOnlyColor() {
        val colorStateList = ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_enabled, android.R.attr.state_enabled)
            ),
            intArrayOf(
                ContextCompat.getColor(context, R.color.color_light_grey),
                ContextCompat.getColor(context, R.color.color_light_grey)
            )
        )
        setBoxStrokeColorStateList(colorStateList)
        boxBackgroundColor = ContextCompat.getColor(context, R.color.color_white)
        autoCompleteTextView.setTextColor(ContextCompat.getColor(context, R.color.color_light_grey))
    }

    override fun setOnClickListener(l: OnClickListener?) {
        super.setOnClickListener(l)
        clickHandling()
    }

    private fun clickHandling() {
        if (isSearchable) {
            val activity = scanForActivity(context)
            activity?.let { openSearchDialogFragment(it) } ?: run {
                Log.d("MaterialSpinner", "activity not found")
            }
        } else autoCompleteTextView.showDropDown()
    }

    private fun initListeners() {
        autoCompleteTextView.onItemClickListener =
            OnItemClickListener { _, _, position, _ ->
                currentSelectedItem = itemsList.getOrNull(position)
                currentSelectedItem?.let { onSelectedItemClickListener?.onItemClick(it) }
            }
    }

    private fun initWatchers() {
        autoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                isErrorEnabled = false
            }
        })
    }

    fun <T> setAdapter(adapter: T) where T : ListAdapter, T : Filterable {
        autoCompleteTextView.setAdapter(adapter)
    }

    fun <T : List<String>> setItems(items: T) {
        if (isSearchable) this.items = items.toList() as? ArrayList<String> ?: ArrayList()
        else setAdapter(ArrayAdapter(context, R.layout.support_simple_spinner_dropdown_item, items))
    }

    fun setItems(items: Array<String>) {
        if (isSearchable) this.items = items.toList() as? ArrayList<String> ?: ArrayList()
        else setAdapter(ArrayAdapter(context, R.layout.support_simple_spinner_dropdown_item, items))
    }

    /**
     * Sets the list of items for the AutoCompleteTextView.
     *
     * @param itemsMap A map where:
     *   - First `String?` are representing item IDs or KEYs.
     *   - Second `String?` are representing item NAME or VALUE.
     *
     * Example: If you have list of object you can pass map like this:
     * ```kotlin
     * val countryList = listOf(
     *     Country("1", "Bangladesh"),
     *     Country("2", "Turkey"),
     *     // ...
     * )
     * val itemsMap = countryList.associate { it.id to it.name }
     * setItems(itemsMap)
     * ```
     */
    fun setItems(itemsMap: Map<String?, String?>) {
        if (isSearchable) {
            this.items = itemsMap.map { it.value ?: "" }.toList() as ArrayList<String>
        } else {
            itemsList = itemsMap.map {
                SelectedItem(it.key, it.value)
            }.toList()
            setAdapter(
                ArrayAdapter(
                    context,
                    R.layout.support_simple_spinner_dropdown_item,
                    itemsMap.map { it.value ?: "" })
            )
        }
    }


    fun setSelection(position: Int) {
        try {
            autoCompleteTextView.setSelection(position)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setSelection(keyOrId: String?) {
        if (itemsList.isEmpty() || keyOrId.isNullOrEmpty()) {
            return
        }
        try {
            autoCompleteTextView.setSelection(itemsList.indexOfFirst { it.key == keyOrId })
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }

    fun clearSelection() {
        try {
            currentSelectedItem = null
            autoCompleteTextView.clearListSelection()
            autoCompleteTextView.setText(null, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun setHint(hint: CharSequence?) {
        if (isRequired) {
            hintForColor = "$hint *"
            setHintAsteriskColor(Color.RED)
        } else {
            hintForColor = hint?.toString() ?: ""
            super.setHint(hint)
        }
    }

    var text: String?
        get() {
            return autoCompleteTextView.text?.toString()
        }
        set(value) {
            autoCompleteTextView.setText(value, false)
        }

    val selectedItem: SelectedItem?
        get() = currentSelectedItem

    fun addTextChangedListener(watcher: TextWatcher) {
        autoCompleteTextView.addTextChangedListener(watcher)
    }

    fun onItemClickListener(listener: AdapterView.OnItemClickListener) {
        autoCompleteTextView.onItemClickListener = listener
    }

    fun setHintAsteriskColor(color: Int) {
        val len = hintForColor.length
        val sb = SpannableStringBuilder(hintForColor)
        val asteriskColor = ForegroundColorSpan(color)
        if (len != 0) {
            sb.setSpan(asteriskColor, len - 1, len, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            super.setHint(sb)
        }
    }

    fun setHintFontFamily(fontFamily: Int) {
        fontFamily.let { ResourcesCompat.getFont(context, it) }.also { typeface = it }
    }

    fun setBoxWidth(size: Int) {
        boxStrokeWidth = size
        boxStrokeWidthFocused = size
    }

    fun setTextFontFamily(fontFamily: Int) {
        autoCompleteTextView.typeface = fontFamily.let { ResourcesCompat.getFont(context, it) }
    }

    fun setTextColor(textColorCode: String) {
        autoCompleteTextView.setTextColor(Color.parseColor(textColorCode))
    }

    fun setFontSize(fontSize: Float) {
        if (fontSize > 0) autoCompleteTextView.textSize = fontSize
    }

    fun setIsRequired(isReq: Boolean) {
        isRequired = isReq
        if (!hint.toString().contains("*"))
            hint = hint?.toString()?.trim()?.replace(" *", "")
    }

    fun setIsSearchable(searchable: Boolean) {
        isSearchable = searchable
    }

    private var searchDialogFragment: SearchDialogFragment? = null

    fun getDialogFragment(): SearchDialogFragment? {
        return searchDialogFragment
    }

    private fun openSearchDialogFragment(activity: Activity) {
        val container = activity.findViewById<View>(android.R.id.content)
        val searchDialogFragment = SearchDialogFragment.newInstance(
            items = items,
            title = searchTitle
        )


        val fragmentActivity = activity as FragmentActivity
        if (container != null) {
            fragmentActivity.supportFragmentManager.beginTransaction()
                .replace(container.id, searchDialogFragment).commit()
        }

        fragmentActivity.supportFragmentManager.setFragmentResultListener(
            KEY_SEARCH,
            fragmentActivity
        ) { _, result ->
            val selectedItem = result.getString(KEY_SELECTED_ITEM)
            text = selectedItem
            val position = result.getInt(KEY_SELECTED_POSITION)
            onSearchSpinnerItemClickListener?.onItemClicked(selectedItem, position)
        }
    }

    private fun scanForActivity(ctx: Context): Activity? {
        if (ctx is Activity) return ctx else if (ctx is ContextWrapper) return scanForActivity(
            ctx.baseContext
        )
        return null
    }

    fun setSearchTitle(title: String) {
        searchTitle = title
    }

    fun onSearchSpinnerItemClickListener(listener: OnSearchSpinnerItemClickListener) {
        this.onSearchSpinnerItemClickListener = listener
    }


    /**
     * OnSelectedItemClickListener for the MaterialSpinner.
     *
     * **Important:** To receive data from [OnSelectedItemClickListener], you must
     *  set item using function **setItems(itemsMap: Map<String?, String?>)**. Otherwise, no data will be
     * available from the listener.
     *
     *
     */
    fun onSelectedItemClickListener(listener: OnSelectedItemClickListener) {
        this.onSelectedItemClickListener = listener
    }


}