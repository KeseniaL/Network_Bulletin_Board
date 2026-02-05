
/*Responsible for the following:
    - Parsing clinet commands
    - double checking and validating syntax
    - success or error strings set up
*/

//class handles parsing a single client command
public class ProtocolParser{

    public static String parse(String input){
        if (input == null || input.isEmpty()){
            return error ("INVALID_FORMAT", "Empty command"); //rejects any empty commands immediately
        }

        String[] tokens = input.split ("\\s+"); // split command by spaces, single spaces only as detailed in RFC
        String cmd = tokens[0]; // first token must be command key word
        //parsing for the different commands
        switch (cmd){
            case "POST":
                return parsePost (tokens, input);

            case "GET":
                return "SUCCESS GET (stub)";
            
            case "PIN":
            case "UNPIN":
                    return parsePin(tokens);
            
            case "SHAKE":
            case "CLEAR":
                 return ParseNoArgs(tokens, cmd);

            case "DISCONNECT":
                return "SUCCESS DISCONNECTED";
            
            default:
                return error("INVALID_FORMAT", "Unknown command");
        }
    }
    //this validates syntax of POST <x> <y> <colour> <message>
    private static String parsePost(String[] tokens, String fulline){
        if (tokens.length < 5){
            return error("INVALID_FORMAT", "POST requires x y colour message");
        }
        try {
            Integer.parseInt(tokens[1]);
            Integer.parseInt(tokens[2]);
        }catch (NumberFormatException e){
            return error("INVALID_FORMAT", "Coordinates must be non negative integers"); //coordinates has to be non negative int
        }
        //validate colour
        String colour = tokens[3].toLowerCase();
        if (!BBoard.VALID_COLOURS.contains(colour)){
            return error("COLOUR_NOT_SUPPORTED","Colour not found in list");
        }
        return "SUCCESS POST_PARSED";
    }
    //validates PIN and UNPIN syntax: PIN <x> <y>
    private static String parsePin(String[] tokens){
        if (tokens.length != 3){
            return error("INVALID_FORMAT", "PIN requires x y coordinates");
        }

        try {
            Integer.parseInt(tokens[1]);
            Integer.parseInt(tokens[2]);
        } catch (NumberFormatException e){
            return error("INVALID_FORMAT", "Coordinates must be non negative integers");
        }

        return "SUCCESS PIN_PARSED"; //succesful parsing pin/unpin
    }
    //for SHAKE and CLEAR that takes no arguments
    private static String ParseNoArgs(String[] tokens, String cmd){
        if (tokens.length != 1){
            return error("INVALID_FORMAT", cmd + "takes no arguments");
        }
        return "SUCCESS" + cmd + "_PARSED";
    }
    //standardizes error messages from server
    private static String error (String code, String msg){
        return "ERROR" + code + " " + msg;
    }
}