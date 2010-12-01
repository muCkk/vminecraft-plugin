import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

//=====================================================================
//Class:	vMinecraftChat
//Use:		Encapsulates all chat commands added by this mod
//Author:	nossr50, TrapAlice, cerevisiae
//=====================================================================
public class vMinecraftChat {
    protected static final Logger log = Logger.getLogger("Minecraft");

	//=====================================================================
	//Function:	gmsg
	//Input:	String msg: The message to be broadcast to all players
	//Output:	None 
	//Use:		Outputs a message to everybody
	//=====================================================================
    public static void gmsg(String msg){
        for (Player p : etc.getServer().getPlayerList()) {
            if (p != null) {
                p.sendMessage(msg);
            }
        }
    }

	//=====================================================================
	//Function:	wordWrap
	//Input:	String msg: The message to be wrapped
	//Output:	String[]: The array of substrings 
	//Use:		Cuts the message apart into whole words short enough to fit
    //			on one line
	//=====================================================================
    public static String[] wordWrap(String msg){
    	//Split each word apart
    	String[] split = msg.split(" ");
    	
    	//Create an arraylist for the output
    	ArrayList<String> out = new ArrayList<String>();
    	
    	//While i is less than the length of the array of words
    	int i = 0;
    	while(i < split.length){
    		int len = 0;
    		int j = i;
    		
    		//Loop through the words finding their length and increasing
    		//j, the end point for the sub string
    		while(len <= 316 && i < split.length)
    		{
    			len += msgLength(split[i]) + 4;
    			if( len <= 316)
    				i++;

    		}
			//Copy the words in the selection into a new array
    		String[] temp = new String[i - j];
    		System.arraycopy(split, j, temp, 0, i - j);

    		//Merge them and add them to the output array
    		out.add( etc.combineSplit(0, temp, " ") );
    	}
    	
    	//Convert to an array and return
    	String[] tempout = new String[out.size()];
    	out.toArray(tempout);
    	return tempout;
    }
    
	//=====================================================================
	//Function:	msgLength
	//Input:	String str: The string to find the length of
	//Output:	int: The length on the screen of a string
	//Use:		Finds the length on the screen of a string. Ignores colors.
	//=====================================================================
    private static int msgLength(String str){
		int length = 0;
		//Loop through all the characters, skipping any color characters
		//and their following color codes
		for(int x = 0; x<str.length(); x++)
		{
			if(str.charAt(x) == '�')
				x++;
			else if("i;,.:|!".indexOf(str.charAt(x)) != -1)
				length+=2;
			else if("l'".indexOf(str.charAt(x)) != -1)
				length+=3;
			else if("tI[]".indexOf(str.charAt(x)) != -1)
				length+=4;
			else if("kf{}<>\"*()".indexOf(str.charAt(x)) != -1)
				length+=5;
			else if("hequcbrownxjmpsvazydgTHEQUCKBROWNFXJMPSVLAZYDG1234567890#\\/?$%-=_+&".indexOf(str.charAt(x)) != -1)
				length+=6;
			else if("@~".indexOf(str.charAt(x)) != -1)
				length+=7;
			else if(str.charAt(x)==' ')
				length+=4;
		}
		return length;
    }

	//=====================================================================
	//Function:	rainbow
	//Input:	String msg: The string to colorify
	//Output:	String: The rainbowed result
	//Use:		Rainbowifies a string;
	//=====================================================================
    public static String rainbow(String msg){
    	String temp = "";
    	//The array of colors to use
		String[] rainbow = new String[] {Colors.Red, Colors.Rose, Colors.Gold,
				Colors.Yellow, Colors.LightGreen, Colors.Green, Colors.Blue,
				Colors.Navy, Colors.DarkPurple, Colors.Purple, Colors.LightPurple};
		int counter=0;
		//Loop through the message applying the colors
		for(int x=0; x<msg.length(); x++)
		{
			temp+=rainbow[counter]+msg.charAt(x);
			
			if(msg.charAt(x)!=' ') counter++;
			if(counter==rainbow.length) counter = 0;
		}
		return temp;
    }
	//=====================================================================
	//Function:	getName
	//Input:	Player player: The player to get name as color
	//Output:	String: The name colored 
	//Use:		Returns the colored name;
	//=====================================================================
    public static String getName(Player player){
    	
    	//Get the prefix
    	String playerPrefix = player.getPrefix();
    	
    	//Add the name
    	String output = player.getName();
    	
    	//Add the color if there is one
    	if(player.getColor() != null && player.getColor() != "")
    		output = player.getColor().substring(0,2) + output;
    	//Add the prefix if there is one
    	if(playerPrefix != null && playerPrefix != "")
    		output = applyColors(playerPrefix).substring(3) + output;
    	
    	//Return the name
        return output;
    }
    
	//=====================================================================
	//Function:	colorChange
	//Input:	char colour: The color code to find the color for
	//Output:	String: The color that the code identified 
	//Use:		Finds a color giving a color code
	//=====================================================================
	public static String colorChange(char colour)
	{
		String color = "";
		switch(colour)
		{
			case '0':
				color = Colors.Black;
				break;
			case '1':
				color = Colors.Navy;
				break;
			case '2':
				color = Colors.Green;
				break;
			case '3':
				color = Colors.Blue;
				break;
			case '4':
				color = Colors.Red;
				break;
			case '5':
				color = Colors.Purple;
				break;
			case '6':
				color = Colors.Gold;
					break;
			case '7':
				color = Colors.LightGray;
				break;
			case '8':
				color = Colors.Gray;
				break;
			case '9':
				color = Colors.DarkPurple;
				break;
			case 'a':
				color = Colors.LightGreen;
				break;
			case 'b':
				color = Colors.LightBlue;
				break;
			case 'c':
				color = Colors.Rose;
				break;
			case 'd':
				color = Colors.LightPurple;
				break;
			case 'e':
				color = Colors.Yellow;
				break;
			case 'f':
				color = Colors.White;
				break;
			case 'A':
				color = Colors.LightGreen;
				break;
			case 'B':
				color = Colors.LightBlue;
				break;
			case 'C':
				color = Colors.Rose;
				break;
			case 'D':
				color = Colors.LightPurple;
				break;
			case 'E':
				color = Colors.Yellow;
				break;
			case 'F':
				color = Colors.White;
				break;
			default:
				color = null;
				break;
		}
		return color;
	}
	  
	//=====================================================================
	//Function:	adminChat
	//Input:	Player player: The player talking
    //			String message: The message to apply the effect to
	//Output:	boolean: If this feature is enabled
	//Use:		Sends messages only to admins
	//=====================================================================
	public static boolean adminChat(Player player, String message){
		
		//Check if the player can use this feature
		if(player.isAdmin() || player.canUseCommand("/adminchat"))
		{
			//Special formatting for adminchat {Username}
	        String adminchat = Colors.DarkPurple + "{" + getName(player)
	        +  Colors.DarkPurple +"}" + Colors.White + " ";
	        
	        String[] msg = wordWrap(adminchat + message.substring(1, message.length()));
	        
	        //Get the player from the playerlist to send the message to.
			for (Player p: etc.getServer().getPlayerList()) {
				
				//If p is not null
				if (p != null) {
					
					//And if p is an admin or has access to adminchat
					if (p.isAdmin() || (p.canUseCommand("/adminchat"))) {

						//Output the first line
						for(String str: msg)
							p.sendMessage(str);
					}
				}
			}

		    //So you can read adminchat from the server console
			log.log(Level.INFO, "@" + "<" + getName(player)
					+  Colors.White +"> " + message); 
			return true;
		}
		return false;
	}
        public static boolean adminChatToggle(Player player, String message){
            if(vMinecraftSettings.getInstance().isAdminToggled(player.getName())) {
                String adminchat = Colors.DarkPurple + "{" + getName(player)
	        +  Colors.DarkPurple +"}" + Colors.White + " ";
                String[] msg = wordWrap(adminchat + message.substring(1, message.length()));
                for (Player p: etc.getServer().getPlayerList()) {
                    if (p != null) {
                        if (p.isAdmin() || p.canUseCommand("/adminchat")) {
                            for(String str: msg)
                                p.sendMessage(str);
                        }
                    }
                }
                log.log(Level.INFO, "@" + "<" + getName(player)
					+  Colors.White +"> " + message);
                return true;
            }
            return false;
        }

	//=====================================================================
	//Function:	quote
	//Input:	Player player: The player talking
    //			String message: The message to apply the effect to
	//Output:	boolean: If this feature is enabled
	//Use:		Displays a message as a quote
	//=====================================================================
	public static boolean quote(Player player, String message)
	{
		//Format the name
		String playerName = Colors.White + "<" + getName(player)
				+ Colors.White + "> ";
		if(vMinecraftSettings.getInstance().greentext()) {
			//Log the chat
			log.log(Level.INFO, "<"+player.getName()+"> " +message);
	        
			//Get the multi line array
	        String[] msg = wordWrap(playerName + Colors.LightGreen + message);

			//Output the lines
			for(String str: msg)
				gmsg(Colors.LightGreen + str);
			return true;
		}
		return false;
	}

	//=====================================================================
	//Function:	rage
	//Input:	Player player: The player talking
    //			String message: The message to apply the effect to
	//Output:	boolean: If this feature is enabled
	//Use:		Displays a message in red
	//=====================================================================
	public static boolean rage(Player player, String message)
	{
		//Format the name
		String playerName = Colors.White + "<"
				+ getName(player) + Colors.White +"> ";
		if (vMinecraftSettings.getInstance().FFF()) {
			log.log(Level.INFO, "<"+player.getName()+"> "+message);
	        
			//Get the multi line array
	        String[] msg = wordWrap(playerName + Colors.Red +  message);

			//Output the message
			for(String str: msg)
				gmsg(Colors.Red + str);
			return true;
		}
		return false;
	}
    
    //=====================================================================
	//Function:	quakeColors
	//Input:	Player player: The player talking
    //			String message: The message to apply the effect to
	//Output:	boolean: If this feature is enabled
	//Use:		Displays a message in red
	//=====================================================================
	public static boolean quakeColors(Player player, String message)
	{
		//Format the name
		String playerName = Colors.White + "<"
		+ getName(player) + Colors.White +"> ";
		if(vMinecraftSettings.getInstance().quakeColors()) {

			//Log the chat
			log.log(Level.INFO, "<"+player.getName()+"> "+message);
			
			//Get the multi line array
	        String[] msg = wordWrap(playerName + message);
	        //Apply colors to the lines
			applyColors(msg);

			//Output the message
			for(String str: msg)
				gmsg(str);

			//Loop through the string finding the color codes and inserting them
			return true;
		}
		return false;
	}

    
    //=====================================================================
	//Function:	applyColors
	//Input:	String[] message: The lines to be colored
	//Output:	String[]: The lines, but colorful
	//Use:		Colors each line
	//=====================================================================
	public static String[] applyColors(String[] message)
	{
		if(message != null && message[0] != null && !message[0].equals("")){
			//The color to start the line with
			String recentColor = Colors.White;
			
			//Go through each line
			int counter = 0;
			for(String msg: message)
			{	
				//Start the line with the most recent color
				String temp = recentColor;
				
				//Loop through looking for a color code
				for(int x = 0; x< msg.length(); x++)
				{
					//If the char is a ^ or �
					if(msg.charAt(x) == '^' || msg.charAt(x) == '�')
					{
						if(x != msg.length() - 1)
						{
							//If the following character is a color code
							if(vMinecraftChat.colorChange(msg.charAt(x+1)) != null)
							{
								//Set the most recent color to the new color
								recentColor = vMinecraftChat.colorChange(msg.charAt(x+1));
								//Add the color
								temp += recentColor;
								//Skip these chars
								x++;
							//Otherwise ignore it.
							} else {
								temp += msg.charAt(x);
							}
						//Insert the character
						}
					} else {
						temp += msg.charAt(x);
					}
				}
				//Replace the message with the colorful message
				message[counter] = temp;
				counter++;
			}
		}
		return message;
	}

	//=====================================================================
	//Function:	applyColors
	//Input:	String message: The line to be colored
	//Output:	String: The line, but colorful
	//Use:		Colors a line
	//=====================================================================
	public static String applyColors(String message)
	{
		return applyColors(message, Colors.White);
	}

	//=====================================================================
	//Function:	applyColors
	//Input:	String message: The line to be colored
	//			String color: The color to start the line with
	//Output:	String: The line, but colorful
	//Use:		Colors a line
	//=====================================================================
	public static String applyColors(String message, String color)
	{
		if(message != null && !message.equals(""))
		{
			//The color to start the line with
			if(color == null)
				 color = Colors.White;
			
			//Start the line with the most recent color
			String temp = color;
			
			//Loop through looking for a color code
			for(int x = 0; x< message.length(); x++)
			{
				//If the char is a ^ or �
				if(message.charAt(x) == '^' || message.charAt(x) == '�')
				{
					if(x != message.length() - 1)
					{
						//If the following character is a color code
						if(vMinecraftChat.colorChange(message.charAt(x+1)) != null)
						{
							//Set the most recent color to the new color
							color = vMinecraftChat.colorChange(message.charAt(x+1));
							//Add the color
							temp += color;
							//Skip these chars
							x++;
						//Otherwise ignore it.
						} else {
							temp += message.charAt(x);
						}
					//Insert the character
					} else {
						temp += message.charAt(x);
					}
				}
	
			}
		}
		return message;
	}
}