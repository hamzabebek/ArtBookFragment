package com.example.artbookfragment.artbooknavigation.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.room.Room
import com.example.artbookfragment.R
import com.example.artbookfragment.artbooknavigation.model.Art
import com.example.artbookfragment.artbooknavigation.roomdb.ArtDao
import com.example.artbookfragment.artbooknavigation.roomdb.ArtDatabase
import com.example.artbookfragment.databinding.FragmentDetailBinding
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import java.io.IOException


class DetailFragment : Fragment() {

        var selectedPicture : Uri? = null
        var selectedBitmap : Bitmap? = null
        private var _binding : FragmentDetailBinding? = null
        private val binding get() = _binding!!
        private lateinit var activityResultLauncer : ActivityResultLauncher<Intent>
        private lateinit var permissionLauncher : ActivityResultLauncher<String>
        private lateinit var artDatabase : ArtDatabase
        private lateinit var artDao : ArtDao
        private val mDisposable = CompositeDisposable()
    var artFromMain : Art? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLauncer()
        artDatabase = Room.databaseBuilder(requireContext(),ArtDatabase::class.java,"Arts").build()
        artDao = artDatabase.ArtDao()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.saveButton.setOnClickListener{ save(view) }
        binding.deleteButton.setOnClickListener { delete(view) }
        binding.imageView.setOnClickListener { selectImage(view) }

        arguments?.let {
            val info = DetailFragmentArgs.fromBundle(it).info
            if (info.equals("new")){
                //NEW
                binding.artNameText.setText("")
                binding.artistNameText.setText("")
                binding.yearText.setText("")
                binding.saveButton.visibility = View.VISIBLE
                binding.deleteButton.visibility = View.GONE
                val selectedImageBackground = BitmapFactory.decodeResource(context?.resources, R.drawable.selectimage)
                binding.imageView.setImageBitmap(selectedImageBackground)
            }else{
                //OLD
                binding.saveButton.visibility = View.GONE
                binding.deleteButton.visibility = View.VISIBLE

                val selectedId = DetailFragmentArgs.fromBundle(it).selectedId
                mDisposable.add(artDao.getArtById(selectedId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponseWithOldArt))
            }
        }
    }
    fun delete(view : View){
        artFromMain?.let {
            mDisposable.add(artDao.delete(it)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse))
        }


    }
    fun save(view: View){
        val artName = binding.artNameText.text.toString()
        val artistName = binding.artistNameText.text.toString()
        val year = binding.yearText.text.toString()

        if (selectedBitmap != null){
            val smallBitmap = makeSmallerBitmap(selectedBitmap!!,500)

            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG,100,outputStream)
            val byteArray = outputStream.toByteArray()

            val art = Art(artName,artistName,year,byteArray)

            mDisposable.add(artDao.insert(art)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse))
        }


    }
    fun selectImage(view: View){
        activity?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                if(ContextCompat.checkSelfPermission(requireActivity().applicationContext,Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
                    if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),Manifest.permission.READ_MEDIA_IMAGES)){
                        Snackbar.make(view,"Permission needed for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission",View.OnClickListener {
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }).show()
                    }else{
                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    }
                }else{
                    val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncer.launch(intentToGallery)
                }
            }else {
                if (ContextCompat.checkSelfPermission(
                        requireActivity().applicationContext,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            requireActivity(),
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                    ) {
                        Snackbar.make(
                            view,
                            "Permission needed for gallery",
                            Snackbar.LENGTH_INDEFINITE
                        ).setAction("Give Permission",
                            View.OnClickListener {
                                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                            }).show()
                    } else {
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                } else {
                    val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        activityResultLauncer.launch(intentToGallery)
                }
            }
        }

    }

    private fun handleResponse(){
        val action = DetailFragmentDirections.actionDetailFragmentToArtList()
        Navigation.findNavController(requireView()).navigate(action)
    }
    private fun handleResponseWithOldArt(art: Art){
        artFromMain = art
        binding.artNameText.setText(art.artName)
        binding.artistNameText.setText(art.artistName)
        binding.yearText.setText(art.year)
        art.image?.let {
            val bitmap = BitmapFactory.decodeByteArray(it,0,it.size)
            binding.imageView.setImageBitmap(bitmap)
        }
    }
    fun makeSmallerBitmap(image : Bitmap, maximumSize : Int) : Bitmap {
        var width = image.width
        var height = image.height

        var bitmapRadio : Double = width.toDouble() / height.toDouble()
        if (bitmapRadio > 1){
            width = maximumSize
            val scaledHeight = width / bitmapRadio
            height = scaledHeight.toInt()
        }else{
            height = maximumSize
            val scaledWidth = height * bitmapRadio
            width = scaledWidth.toInt()
        }
        return Bitmap.createScaledBitmap(image,width,height,true)
    }
    private fun registerLauncer(){
        activityResultLauncer = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
            if(result.resultCode == AppCompatActivity.RESULT_OK){
                val intentFromResult = result.data
                if(intentFromResult != null){
                    selectedPicture = intentFromResult.data
                    try {
                        if(Build.VERSION.SDK_INT >= 28){
                            val source = ImageDecoder.createSource(requireActivity().contentResolver,selectedPicture!!)
                            selectedBitmap = ImageDecoder.decodeBitmap(source)
                            binding.imageView.setImageBitmap(selectedBitmap)
                        }else{
                            selectedBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver,selectedPicture)
                            binding.imageView.setImageBitmap(selectedBitmap)
                        }
                    }catch (e: IOException){
                        e.printStackTrace()
                    }
                }
            }
        }
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){result ->
            if(result){
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncer.launch(intentToGallery)
            }else{
                Toast.makeText(requireContext(),"Permission Needed ! ",Toast.LENGTH_SHORT).show()
                }
            }
        }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    }
