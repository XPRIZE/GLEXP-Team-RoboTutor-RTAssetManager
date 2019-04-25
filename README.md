# RTAssetManager

This is the source code for *RTAssetManager.jar*, which is used to generate hashed filenames of audio files and then push audio files onto a tablet.

Note that the use of this code is a legacy design feature that is being phased out.


### Building a jar
- The code should be compiled into *RTAssetManager.jar*, with the *main* method contained in [AssetManager.java](src/cmu/xprize/AssetManager.java).
- The code is dependent on the [json](lib/json-20140107.jar) and [gson](lib/gson-2.6.2.jar) libraries. These should be placed in the same directory as *RTAssetManager.jar*.


### Running the jar

To hash the audio filenames and PUSH directly to a tablet device, run `java -jar RTAssetManager.jar cmd=build_dist:src=Fake_Audio:compress=false`.

To hash the audio filenames and ZIP into a single .zip folder, run `java -jar RTAssetManager.jar cmd=build_dist:src=Fake_Audio:compress=true`.