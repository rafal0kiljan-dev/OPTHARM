package gen;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class InGen {

    int numStartPopulity = 3400;
    long timeMili = 300000;
    int numSatalites = 40;

    public int getNumStartPopulity() {
        JsonObject obj = loadJsonObjectFromFile("demo/gen_input.json");
        if (obj != null) {
            JsonObject genSetup = obj
                    .getAsJsonObject("genSetup");

            numStartPopulity = genSetup
                    .get("numStartPopulity")
                    .getAsInt();
        }
        return numStartPopulity;
    }

    public long getTimeMili() {
        JsonObject obj = loadJsonObjectFromFile("demo/gen_input.json");
        if (obj != null) {
            JsonObject genSetup = obj
                    .getAsJsonObject("genSetup");

            timeMili = genSetup
                    .get("timeMili")
                    .getAsLong();
        }
        return timeMili;
    }

    public int getNumSatalites() {
        JsonObject obj = loadJsonObjectFromFile("demo/gen_input.json");
        if (obj != null) {
            JsonObject genSetup = obj
                    .getAsJsonObject("genSetup");
            numSatalites = genSetup
                    .get("numSatalites")
                    .getAsInt();
        }
        return numSatalites;
    }

    private JsonObject loadJsonObjectFromFile(String filename) {
        FileReader fileReader = null;
        try {
            File gen_input = new File(filename);
            fileReader = new FileReader(gen_input);
            Gson gson = new Gson();
            return gson.fromJson(fileReader, JsonObject.class);
        } catch (FileNotFoundException e) {
            System.out.println("Don't find file " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static void main(String[] args) {
        InGen inGen = new InGen();
        int numStartPopulity = inGen.getNumStartPopulity();
        long timeMili = inGen.getTimeMili();
        int numSatalites = inGen.getNumSatalites();
        // System.out.println("numStartPopulity: " + numStartPopulity);
        // System.out.println("numGeneration: " + timeMili);
        // System.out.println("numGeneration: " + numSatalites);
    }
}
