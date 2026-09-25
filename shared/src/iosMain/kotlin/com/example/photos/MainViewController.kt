package com.example.photos

import androidx.compose.ui.window.ComposeUIViewController
import com.example.photos.di.platformModule
import com.example.photos.data.picker.IosPhotoPicker
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    lateinit var controller: UIViewController
    val picker = IosPhotoPicker { controller }
    val dependencies = platformModule(picker)
    controller = ComposeUIViewController { App(dependencies) }
    return controller
}
