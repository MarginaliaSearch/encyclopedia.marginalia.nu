package nu.marginalia.encyclopedia;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class MainSelector {
    private static final Logger logger = LoggerFactory.getLogger(MainSelector.class);

    public static void main(String[] args) {
        String mode;
        String[] params;

        if (args.length > 0) {
            mode = args[0];
            params = Arrays.copyOfRange(args, 1, args.length);
        }
        else {
            mode = "";
            params = new String[0];
        }

        try {
            invoke(mode, params);
        }
        catch (Exception ex) {
            logger.error("Caught error in main", ex);
        }
    }

    private static void invoke(String mode, String[] params) throws Exception {
        switch (mode) {
            case "serve":
                EncyclopediaServiceMain.main(params);
                break;
            case "convert":
                ConvertZimFileMain.main(params);
                break;
            default:
                System.out.println("Usage modes: ");
                System.out.println("convert /path/to/file.zim /path/to/file.db");
                System.out.println("serve port /path/to/file.db");
        }
    }
}
