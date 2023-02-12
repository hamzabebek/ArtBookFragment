package com.example.artbookfragment.artbooknavigation.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.artbookfragment.R
import com.example.artbookfragment.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var navigationController : NavController

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        navigationController = Navigation.findNavController(this,R.id.fragment)
        NavigationUI.setupActionBarWithNavController(this,navigationController)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //Inflater
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.menu,menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.fragment)
        return navController.navigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.add_art){
            val action = ArtListDirections.actionArtListToDetailFragment("new",0)
            Navigation.findNavController(this,R.id.fragment).navigate(action)
        }

        return super.onOptionsItemSelected(item)
    }
}