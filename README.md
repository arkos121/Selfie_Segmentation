# ML Kit Selfie Segmentation & Object Detection App

## 📸 Overview
This Android application leverages **Google's ML Kit** to provide advanced **Selfie Segmentation** and **Object Detection** capabilities. Users can:

- Upload an image from their **gallery** 📂
- **Remove the background** of the uploaded image 🎭
- **Add different background images** to replace the original 📷
- **Multitouch pan & zoom** on the foreground image 🖐️🔍
- **Add emojis** anywhere on the image, just like Instagram stories 😃🎨

## ✨ Features
- **Selfie Segmentation**: Automatically remove the background of a person in an image.
- **Object Detection**: Detect and interact with objects in an image.
- **Background Replacement**: Choose and apply different backgrounds after segmentation.
- **Pan & Zoom**: Use multitouch gestures to move and zoom the foreground.
- **Emoji Overlay**: Add multiple emojis anywhere in the photo for customization.

## 🚀 Technologies Used
- **Kotlin** 🟣
- **ML Kit (Selfie Segmentation, Object Detection)** 🤖
- **Android Jetpack (ViewModel, LiveData, etc.)** 🔧
- **Canvas & Bitmap Processing** 🎨
- **Gesture Detection (Multi-touch)** ✋
- **Glide (for image loading)** 📷


## 🔧 Implementation Details
### **1. Image Upload and Display**
- Users select an image from the gallery.
- The image is loaded using **Glide**.
- Bitmap processing is used to manage the image layers.

### **2. Selfie Segmentation with ML Kit**
- ML Kit's **Selfie Segmentation** API extracts the foreground (person) from the background.
- The segmented foreground is converted into a transparent **Bitmap**.

### **3. Background Replacement**
- Users select a new background.
- The foreground image is overlaid on the chosen background.

### **4. Multi-touch Gestures for Zoom & Pan**
- The app supports **ScaleGestureDetector** and **GestureDetector** to enable pinch-to-zoom and panning.
- The foreground image can be moved and scaled smoothly.

### **5. Adding Emojis**
- Users can select an emoji from a list.
- The emoji is placed on the image and can be resized or moved using gestures.

### **6. Saving and Sharing the Edited Image**
- The final image is merged using **Canvas**.
- The result can be saved to local storage or shared on social media.




