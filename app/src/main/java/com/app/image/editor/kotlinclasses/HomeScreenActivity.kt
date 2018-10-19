package com.app.image.editor.kotlinclasses

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.View
import com.app.image.editor.R
import com.app.image.editor.javaclasses.Activities.ImageEditingActivity
import com.app.image.editor.javaclasses.Adapters.ImagesAdapter
import com.app.image.editor.javaclasses.Interfaces.OnItemClickListener
import com.app.image.editor.javaclasses.Utils.GetImagePathFromUri
import com.app.image.editor.javaclasses.models.Constants
import com.app.image.editor.javaclasses.models.ImageModel
import kotlinx.android.synthetic.main.activity_home_screen.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.channels.FileChannel

class HomeScreenActivity : AppCompatActivity(), View.OnClickListener {

    val perms = arrayOf("android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.CAMERA")
    val REQUEST_CODE = 200
    val PICK_IMAGE_REQUEST_CODE = 150
    private lateinit var rootFolder: File
    lateinit var imagesList: ArrayList<ImageModel>
    private val TAKE_PICTURE = 1
    private lateinit var imageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)

        if (ActivityCompat.checkSelfPermission(this@HomeScreenActivity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this@HomeScreenActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(perms, REQUEST_CODE)
            }
        } else {
            home_screen_browse_pics_textview.setOnClickListener(this)
            home_screen_camera_textview.setOnClickListener(this)
            getEditedImages()
        }
    }


    override fun onResume() {
        super.onResume()
        getEditedImages()
    }

    fun getEditedImages() {
        imagesList = java.util.ArrayList()
        val options = BitmapFactory.Options()
        options.inSampleSize = 8
        val imagesFolder = File(Constants.APP_FOLDER_PATH)
        if (imagesFolder.exists()) {
            for (singleFile in imagesFolder.listFiles()) {
                val image = BitmapFactory.decodeFile(singleFile.absolutePath)
                if (image != null) {
                    imagesList.add(ImageModel(image, singleFile))
                }
            }

//            for (i in imagesList.indices) {
//                Log.e("HELLO", "The file is: " + imagesList[i].imageFile?.absolutePath)
//            }
            if (imagesList.size != 0) {
                place_holder.visibility = View.GONE
                home_screen_last_edited_pics.visibility = View.VISIBLE
                val gridLayoutManager = GridLayoutManager(this@HomeScreenActivity, 2)
                gridLayoutManager.orientation = GridLayoutManager.VERTICAL

                val imagesAdapter = ImagesAdapter(this@HomeScreenActivity, imagesList, OnItemClickListener { view, position ->
                    val intent = Intent(this@HomeScreenActivity, ImageEditingActivity::class.java)
                    intent.putExtra("image_path", imagesList[position].imageFile?.absolutePath)
                    startActivity(intent)
                })

                home_screen_last_edited_pics.layoutManager = gridLayoutManager
                home_screen_last_edited_pics.isNestedScrollingEnabled = false
                home_screen_last_edited_pics.adapter = imagesAdapter
            } else {
                place_holder.visibility = View.VISIBLE
                home_screen_last_edited_pics.visibility = View.GONE
            }
        } else {
            place_holder.visibility = View.VISIBLE
            home_screen_last_edited_pics.visibility = View.GONE
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                grantResults[2] == PackageManager.PERMISSION_GRANTED) {
            home_screen_browse_pics_textview.setOnClickListener(this)
            home_screen_camera_textview.setOnClickListener(this)
            getEditedImages()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(perms, REQUEST_CODE)
            }
        }
    }

    override fun onClick(v: View?) {
        if (v != null) {
            rootFolder = File(Constants.APP_FOLDER_PATH)
            if (!rootFolder.exists()) {
                rootFolder.mkdir()
            }
            when (v.id) {
                R.id.home_screen_browse_pics_textview -> {
                    Log.e("TAG", "Browse tapped")
                    val intent = Intent()
                    intent.type = "image/*"
                    intent.action = Intent.ACTION_GET_CONTENT
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST_CODE)
                }
                R.id.home_screen_camera_textview -> {
                    Log.e("TAG", "Camera tapped")
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    val photo = File(Constants.APP_FOLDER_PATH, "image_edit_" + (rootFolder.listFiles().size + 1) + ".jpg")
                    intent.putExtra(MediaStore.EXTRA_OUTPUT,
                            FileProvider.getUriForFile(this@HomeScreenActivity, applicationContext.packageName + ".provider", photo))
                    imageUri = FileProvider.getUriForFile(this@HomeScreenActivity, applicationContext.packageName + ".provider", photo)
                    startActivityForResult(intent, TAKE_PICTURE)
                }
            }
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST_CODE) {
            if (data != null) {
                val uri = data.data //Uri
                val imagePath: String

                if (Build.VERSION.SDK_INT < 19) {
                    imagePath = GetImagePathFromUri.getRealPathFromURI_API11to18(this@HomeScreenActivity, uri)
                } else {
                    imagePath = GetImagePathFromUri.getRealPathFromURI_API19(this@HomeScreenActivity, uri)
                }

                val newFile = File(Constants.APP_FOLDER_PATH, "image_edit_" + (rootFolder.listFiles().size + 1) + ".png")
                if (!newFile.exists()) newFile.createNewFile()
                val sourceFile = File(imagePath)
                CopyFile(sourceFile, newFile)
                val intent = Intent(this@HomeScreenActivity, ImageEditingActivity::class.java)
                intent.putExtra("image_path", newFile.absolutePath)
                startActivity(intent)
            }
        } else if (requestCode == TAKE_PICTURE) {
            val photo = File(Constants.APP_FOLDER_PATH, "image_edit_" + (rootFolder.listFiles().size) + ".jpg")
            val intent = Intent(this@HomeScreenActivity, ImageEditingActivity::class.java)
            intent.putExtra("image_path", photo.absolutePath)
            startActivity(intent)
        }
    }

    @Throws(IOException::class)
    private fun CopyFile(sourceFile: File, destFile: File) {
        if (!sourceFile.exists()) {
            return
        }
        val source: FileChannel? = FileInputStream(sourceFile).channel
        val destination: FileChannel? = FileOutputStream(destFile).channel
        if (destination != null && source != null) {
            destination.transferFrom(source, 0, source.size())
        }
        if (source != null) {
            source.close()
        }
        if (destination != null) {
            destination.close()
        }
    }
}
