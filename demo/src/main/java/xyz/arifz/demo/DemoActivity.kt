package xyz.arifz.demo

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import xyz.arifz.demo.databinding.ActivityDemoBinding
import xyz.arifz.materialspinner.OnSearchSpinnerItemClickListener
import xyz.arifz.materialspinner.OnSelectedItemClickListener
import xyz.arifz.materialspinner.SelectedItem

class DemoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDemoBinding
    val TAG = "DemoActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDemoBinding.inflate(layoutInflater)
        
        setContentView(binding.root)
        setupSpinner()
        setUpWithMap()
        initListener()
    }
    data class Country(val id:String,val name:String)
    private fun setUpWithMap() {
        val countryList = listOf(
            Country("1","Bangladesh"),
            Country("2","Turkey"),
            Country("3","Iraq"),
            Country("4","UAE"),
            Country("5","Dubai"),
            Country("6","Oman")
        )

        binding.spinnerCountry.setItems(countryList.associate { it.id to it.name })
        //binding.spn.setReadOnly(true)
        binding.spinnerCountry.apply {
            setBoxWidth(1)
            setHintFontFamily(R.font.poppins)
            setFontSize(12f)
            setTextColor("#FF0000")
            setTextFontFamily(R.font.poppins)
        }


        binding.spinnerCountry.onSelectedItemClickListener(object :OnSelectedItemClickListener{
            override fun onItemClick(selectedItem: SelectedItem) {
                Log.v(
                    "Hello",
                    "onSelectedItemClickListener $selectedItem"
                )
                Toast.makeText(this@DemoActivity,"OnSeleted $selectedItem",Toast.LENGTH_SHORT)
            }
        })
    }

    private fun setupSpinner() {
        val datas = arrayOf("Male", "Female", "Others","Male", "Female", "Others","Male", "Female", "Others","Male", "Female", "Others","Male", "Female", "Others","Male", "Female", "Others","Male", "Female", "Others","Male", "Female", "Others","Male", "Female", "Others","Male", "Female", "Others","Male", "Female", "Others","Male", "Female", "Others")
        binding.spn.setItems(datas)
        binding.spn.setSearchTitle("Gender")
        binding.spn.hint = "Gender"
        //binding.spn.setReadOnly(true)
        binding.spn.apply {
            setBoxWidth(1)
            setHintFontFamily(R.font.poppins)
            setFontSize(12f)
            setTextColor("#FF0000")
            setTextFontFamily(R.font.poppins)
        }

        binding.spn.text = "Female"

        binding.spn.onItemClickListener { _, _, _, _ ->
            Log.v(
                "Hello",
                "onItemClickListener"
            )
        }
        
        binding.spn.onSearchSpinnerItemClickListener(object: OnSearchSpinnerItemClickListener{
            override fun onItemClicked(item: String?, position: Int) {
                Log.d(TAG, "onItemClicked: $item")
            }
        })

        binding.spn.getDialogFragment()?.dialog?.window?.setLayout(240, 600)
    }

    private fun initListener() {
        binding.btn.setOnClickListener {
            val txt = binding.spn.text
            if (txt.isNullOrEmpty()) {
                Toast.makeText(this, txt ?: "empty", Toast.LENGTH_SHORT).show()
                binding.spn.error = "Error occurred"
            } else {
                binding.spn.text = null
            }

            Toast.makeText(this@DemoActivity,"OnSeleted ${binding.spinnerCountry.selectedItem}",Toast.LENGTH_SHORT).show()
            binding.spn.clearSelection()
        }
    }

    
}