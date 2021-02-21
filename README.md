# minecraft-maps-to-images
Find Minecraft map items and convert them to PNG images.

### Usage
[Download](https://github.com/mircokroon/mineraft-maps-to-images/releases/latest/download/map-to-img.jar) the latest release. Run:

`java -jar map-to-img.jar /path/to/.minecraft/saves/your-world/`

### Example
If you have the following maps in your world:
```
/world/data/map_0.dat
/world/data/map_1.dat
/world/data/map_2.dat
```

And run:
```
java -jar map-to-img.jar world/
```


They will be converted to:
```
/world/out/map_0.png
/world/out/map_1.png
/world/out/map_2.png
```


