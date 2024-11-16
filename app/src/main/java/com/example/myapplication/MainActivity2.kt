package com.example.myapplication

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Adapter
import android.widget.ArrayAdapter

import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity


class MainActivity2 : AppCompatActivity() {


    private lateinit var title: EditText
    private lateinit var description: EditText
    private lateinit var addButton: Button
    private lateinit var readButton: Button
    private lateinit var updateButton: Button
    private lateinit var deleteButton: Button
    private lateinit var listView: ListView

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var adapter: ArrayAdapter<String>
    private var goalList: MutableList<String> = mutableListOf()//list of goals
    private var selectedIndex:Int=-1 //track whick goal is selected for update


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)


        val secondframe = findViewById<Button>(R.id.btnSetTimer)
        secondframe.setOnClickListener {
            val Intent = Intent(this, MainActivity3::class.java)
            startActivity(Intent)

        }

        title = findViewById(R.id.title)
        description = findViewById(R.id.description)
        addButton = findViewById(R.id.btn_Add)
        readButton = findViewById(R.id.btn_Read)
        updateButton = findViewById(R.id.btn_Update)
        deleteButton = findViewById(R.id.btn_Delete)
        listView = findViewById(R.id.allGoals)

        sharedPreferences = this.getSharedPreferences("MySharedPref", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, goalList)
        listView.adapter = adapter


        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedGoal = goalList[position]
            val goalParts = selectedGoal.split("\nDescription: ")

            if (goalParts.size == 2) {
                val selectedTitle = goalParts[0].replace("Title: ", "")
                val selectedDescription = goalParts[1]


                showUpdateDeleteDialog(position, selectedTitle, selectedDescription, editor)
            }
        }

        addButton.setOnClickListener {
            val titleText = title.text.toString()
            val descriptionText = description.text.toString()

            if (titleText.isNotEmpty() && descriptionText.isNotEmpty()) {
                val newGoal = "Title: $titleText\nDescription: $descriptionText"
                goalList.add(newGoal)

                saveGoalsToPreferences(editor)//save updated goal
                clearGoal()

                Toast.makeText(this, "Daily Goal Added", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please enter both Title and Description", Toast.LENGTH_SHORT).show()
            }
        }

        updateButton.setOnClickListener {
            val updateTitle = title.text.toString()
            val updateDescription = description.text.toString()

            if (selectedIndex != -1 && updateTitle.isNotEmpty() && updateDescription.isNotEmpty()) {
                val updatedGoal = "Title: $updateTitle\nDescription: $updateDescription"
                goalList[selectedIndex] = updatedGoal

                saveGoalsToPreferences(editor)
                adapter.notifyDataSetChanged()// change listView

                Toast.makeText(this, "Goal Updated Successfully", Toast.LENGTH_SHORT).show()
                clearGoal()
                selectedIndex = -1//reset selected index
            } else {
                Toast.makeText(this, "Please select a goal to update", Toast.LENGTH_SHORT).show()
            }
        }

        readButton.setOnClickListener {
            displayGoal()
        }
        deleteButton.setOnClickListener {

            goalList.clear()


            editor.clear()
            editor.apply()


            adapter.notifyDataSetChanged()

            Toast.makeText(this, "All Goals Deleted", Toast.LENGTH_SHORT).show()
        }
    }

    //show a dialog box
    private fun showUpdateDeleteDialog(position: Int, selectedTitle: String, selectedDescription: String, editor: SharedPreferences.Editor) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Choose an Option")
        dialogBuilder.setMessage("What do you want to do with the selected goal?")

        dialogBuilder.setPositiveButton("Update") { dialog, _ ->

            title.setText(selectedTitle)
            description.setText(selectedDescription)
            selectedIndex = position
            dialog.dismiss()

            Toast.makeText(this, "Edit the goal and press update", Toast.LENGTH_SHORT).show()
        }

        dialogBuilder.setNegativeButton("Delete") { dialog, _ ->

            goalList.removeAt(position)
            saveGoalsToPreferences(editor)
            adapter.notifyDataSetChanged()

            Toast.makeText(this, "Goal Deleted", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    private fun saveGoalsToPreferences(editor: SharedPreferences.Editor) {
        val goalSet = sharedPreferences.getStringSet("goalList", mutableSetOf()) ?: mutableSetOf()
        goalSet.clear()
        goalSet.addAll(goalList)
        editor.putStringSet("goalList", goalSet)
        editor.apply()
    }

    private fun clearGoal() {
        title.text.clear()
        description.text.clear()
    }

    private fun displayGoal() {
        val goalSet = sharedPreferences.getStringSet("goalList", emptySet()) ?: emptySet()
        val goalArrayList = ArrayList(goalSet)

        goalList.clear()
        goalList.addAll(goalArrayList)
        adapter.notifyDataSetChanged()

        if (goalList.isNotEmpty()) {
            Toast.makeText(this, "Read Daily Goals", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "No Daily Goals found", Toast.LENGTH_SHORT).show()
        }
    }
}