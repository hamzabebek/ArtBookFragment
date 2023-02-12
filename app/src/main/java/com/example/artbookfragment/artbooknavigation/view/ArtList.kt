package com.example.artbookfragment.artbooknavigation.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.artbookfragment.R
import com.example.artbookfragment.artbooknavigation.adapter.ArtAdapter
import com.example.artbookfragment.artbooknavigation.model.Art
import com.example.artbookfragment.artbooknavigation.roomdb.ArtDao
import com.example.artbookfragment.artbooknavigation.roomdb.ArtDatabase
import com.example.artbookfragment.databinding.FragmentArtListBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class ArtList : Fragment() {

    private lateinit var artAdapter : ArtAdapter
    private var _binding :  FragmentArtListBinding? = null
    private val binding get() = _binding!!
    private val mDisposable = CompositeDisposable()
    private lateinit var artDao : ArtDao
    private lateinit var artDatabase : ArtDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        artDatabase = Room.databaseBuilder(requireContext(),ArtDatabase::class.java,"Arts").build()
        artDao = artDatabase.ArtDao()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentArtListBinding.inflate(layoutInflater,container,false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getFromSQL()
    }
    fun getFromSQL(){
        mDisposable.add(artDao.getArtWithNameAndId()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::handleResponse))
    }
    private fun handleResponse(artList: List<Art>) {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        artAdapter = ArtAdapter(artList)
        binding.recyclerView.adapter = artAdapter
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}