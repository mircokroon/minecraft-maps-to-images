import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import se.llbit.nbt.CompoundTag;
import se.llbit.nbt.NamedTag;
import se.llbit.nbt.Tag;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Usage: java -jar map-to-img.jar [directory]");
            System.out.println("Application will search up to 3 levels deep in given directory, so it can be a world or " +
                    "just the .minecraft/saves folder.");
            System.exit(1);
        }
        String folder = args[0];

        List<Path> mapFiles = Files.find(Paths.get(folder), 3, (path, attr) -> {
            return path.getFileName().toString().matches("map_[0-9]+\\.dat");
        }).collect(Collectors.toList());

        Path out = Paths.get(folder, "out");
        Files.createDirectories(out);
        System.out.println("Writing files to " + out);

        int num = 0;
        for (Path p : mapFiles) {
            try {
                BufferedImage img = new BufferedImage(128, 128, BufferedImage.TYPE_4BYTE_ABGR);
                CompoundTag t = read(p.toFile()).unpack().get("data").asCompound();
                byte[] data = t.get("colors").byteArray();

                for (int x = 0; x < 128; x++) {
                    for (int y = 0; y < 128; y++) {
                        byte input = data[x + y * 128];
                        int colId = (input >>> 2) & 0b111111;
                        byte shader = (byte) (input & 0b11);

                        BasicColor col = BasicColor.colors.get(colId);
                        if (col == null) {
                            System.out.println("Unknown color: " + colId);
                            col = BasicColor.TRANSPARENT;
                        }
                        img.setRGB(x, y, col.shaded(shader));
                    }
                }

                // write image to temp location
                Path outFile = Paths.get(out.toString(), p.getFileName().toString().replace(".dat", ".png"));
                Path tempFile = Paths.get(out.toString(), "temp");
                ImageIO.write(img, "png", tempFile.toFile());

                // overwrite file only if its identical, otherwise rename the current file
                if (outFile.toFile().exists()) {
                    if (!FileUtils.contentEquals(outFile.toFile(), tempFile.toFile())) {
                        long time = System.nanoTime();
                        outFile = Paths.get(outFile.toString().replace(".png", "-" + time + ".png"));
                    }
                }

                Files.move(tempFile, outFile, StandardCopyOption.REPLACE_EXISTING);
                tempFile.toFile().renameTo(outFile.toFile());

                num++;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        System.out.println("Done! Converted " + num + " maps.");
    }

    public static Tag read(File f) throws IOException {
        InputStream input = new FileInputStream(f);
        byte[] fileContent = IOUtils.toByteArray(input);
        return NamedTag.read(
                new DataInputStream(new ByteArrayInputStream(gzipDecompress(fileContent)))
        );
    }

    // Source: https://stackoverflow.com/a/44922240
    public static byte[] gzipDecompress(byte[] compressedData) {
        byte[] result = new byte[]{};
        try (ByteArrayInputStream bis = new ByteArrayInputStream(compressedData);
             ByteArrayOutputStream bos = new ByteArrayOutputStream();
             GZIPInputStream gzipIS = new GZIPInputStream(bis)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipIS.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            result = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

}

