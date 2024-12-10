Implementation Notes
Architecture Used - MVVM
Storing images in- Room db, schema - original_image, canonical_image, tags

Canonical image is used to displaying and editing tags

Once permission is granted, scanning for image with photo is done, and the same is inserted in the db
Db has flow attached to it, any entry or modification will be conveyed to the UI, hence with first image in db, 
spinner will stop and image would start displaying in grid format
Using mediapipe, red color box is also put on faces while saving in db as canonical image

Click on any image will open the Image Detail page, in which the image would be seen, where user can tag the face,
same value will be persisted in room db

Structure used -
Global -
    - Appmodules for hilt
    - Converters for storing list of tags in room db
    - Application class

Helper-
    - Image Utils for bitmap, storing and converting images
    - Extension function

Repository
    - Since no external API is involved, hence db - dao, entity and repository is used here
UI
    - Activity, Viewmodel and adapter
Worker
    - <Not implemented> This would be for periodic update of images from room db to server


Some tweaks-
    - Saving images in download folder (canonical images) - this need to change and also keeping 
    room db in sync. In current implementation app would crash if image is deleted from device, need
    to clean up in db also whenever any image referenced is deleted it should reflect here
    - Tagging- Tagging may not work, as touch defined in the radius need to be adjusted as per device
    - For assignment, and lack of time, saving image on every session, resulting in duplicated images 
    for new session, can be avoided
    

Challenges-
1. Mediapipe usage- it is in beta, attributes used one month ago are not supported now
2. JNI and ndk- It enhances app but also slows it down when it comes to hardware rendering
3. Tagging images- need to compute properly inset radius so that images can be tagged efficiently
4. tflite model- Whenever we have tflite, it comes to accuracy %, currently it is set to 0.5f not so accurate
5. Growing size- Images saved in room need to be purged periodically as they are synced with server
   Otherwise size of app would increase quite rapidly
