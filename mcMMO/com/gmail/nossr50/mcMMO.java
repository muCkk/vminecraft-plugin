package com.gmail.nossr50;

import com.gmail.nossr50.datatypes.PlayerProfile;
import com.gmail.nossr50.config.*;
import com.gmail.nossr50.party.Party;
import com.gmail.nossr50.skills.*;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijiko.permissions.PermissionHandler;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.entity.Player;


public class mcMMO extends JavaPlugin 
{
	public static String maindirectory = "plugins/mcMMO/"; 
	File file = new File(maindirectory + File.separator + "config.yml");
	public static final Logger log = Logger.getLogger("Minecraft"); 
	private final mcPlayerListener playerListener = new mcPlayerListener(this);
	private final mcBlockListener blockListener = new mcBlockListener(this);
	private final mcEntityListener entityListener = new mcEntityListener(this);
	private final String name = "mcMMO"; 
	public static PermissionHandler PermissionsHandler = null;
	private Permissions permissions;

	//PERFORMANCE DEBUG STUFF
	public long onPlayerRespawn=0, onPlayerQuit=0, onPlayerLogin=0, onPlayerInteract=0,onPlayerJoin=0, onPlayerCommandPreprocess=0, onPlayerChat=0, onBlockDamage=0, onBlockBreak=0, onBlockFromTo=0, onBlockPlace=0,
	onEntityTarget=0, onEntityDeath=0, onEntityDamage=0, onCreatureSpawn=0, bleedSimulation=0, mcTimerx=0;

	public void printDelays()
	{
		log.log(Level.INFO,"####mcMMO PERFORMANCE REPORT####");
		log.log(Level.INFO,"[These are the cumulative milliseconds mcMMO has lagged in the last 40 seconds]");
		log.log(Level.INFO,"[1000ms = 1 second, lower is better]");

		log.log(Level.INFO,"onPlayerRespawn: "+onPlayerRespawn+"ms");
		log.log(Level.INFO,"onPlayerQuit: "+onPlayerQuit+"ms");
		log.log(Level.INFO,"onPlayerLogin: "+onPlayerLogin+"ms");
		log.log(Level.INFO,"onPlayerInteract: "+onPlayerInteract+"ms");
		log.log(Level.INFO,"onPlayerJoin: "+onPlayerJoin+"ms");
		log.log(Level.INFO,"onPlayerCommandPreProcess: "+onPlayerCommandPreprocess+"ms");
		log.log(Level.INFO,"onPlayerChat: "+onPlayerChat+"ms");

		log.log(Level.INFO,"onBlockDamage: "+onBlockDamage+"ms");
		log.log(Level.INFO,"onBlockBreak: "+onBlockBreak+"ms");
		log.log(Level.INFO,"onBlockFromTo: "+onBlockFromTo+"ms");
		log.log(Level.INFO,"onBlockPlace: "+onBlockPlace+"ms");

		log.log(Level.INFO,"onEntityTarget: "+onEntityTarget+"ms");
		log.log(Level.INFO,"onEntityDeath: "+onEntityDeath+"ms");
		log.log(Level.INFO,"onEntityDamage: "+onEntityDamage+"ms");
		log.log(Level.INFO,"onCreatureSpawn: "+onCreatureSpawn+"ms");

		log.log(Level.INFO,"mcTimer (HPREGEN/ETC): "+mcTimerx+"ms");
		log.log(Level.INFO,"bleedSimulation: "+bleedSimulation+"ms");
		log.log(Level.INFO,"####mcMMO END OF PERFORMANCE REPORT####");

		onPlayerRespawn=0; 
		onPlayerQuit=0;
		onPlayerLogin=0;
		onPlayerJoin=0;
		onPlayerInteract=0;
		onPlayerCommandPreprocess=0;
		onPlayerChat=0;
		onBlockDamage=0;
		onBlockBreak=0;
		onBlockFromTo=0;
		onBlockPlace=0;
		onEntityTarget=0;
		onEntityDeath=0;
		onEntityDamage=0;
		onCreatureSpawn=0;
		mcTimerx=0;
		bleedSimulation=0;
	}

	private Timer mcMMO_Timer = new Timer(true); //BLEED AND REGENERATION
	//private Timer mcMMO_SpellTimer = new Timer(true);

	public static Database database = null;
	public Mob mob = new Mob();
	public Misc misc = new Misc(this);
	public Sorcery sorcery = new Sorcery(this);

	//Config file stuff
	LoadProperties config = new LoadProperties();

	public void onEnable() 
	{
		//new File(maindirectory).mkdir();
		config.configCheck();

		Users.getInstance().loadUsers(); //Load Users file

		/*
		 * REGISTER EVENTS
		 */

		PluginManager pm = getServer().getPluginManager();

		//Player Stuff
		pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_LOGIN, playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Priority.Lowest, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Monitor, this);
		pm.registerEvent(Event.Type.PLAYER_RESPAWN, playerListener, Priority.Normal, this);

		//Block Stuff
		pm.registerEvent(Event.Type.BLOCK_DAMAGE, blockListener, Priority.Highest, this);
		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Highest, this);
		pm.registerEvent(Event.Type.BLOCK_FROMTO, blockListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Priority.Normal, this);

		//Entity Stuff
		pm.registerEvent(Event.Type.ENTITY_TARGET, entityListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.ENTITY_DEATH, entityListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Priority.Highest, this);
		pm.registerEvent(Event.Type.CREATURE_SPAWN, entityListener, Priority.Normal, this);

		PluginDescriptionFile pdfFile = this.getDescription();
		mcPermissions.initialize(getServer());

		if(LoadProperties.useMySQL)
		{
			database = new Database(this);
			database.createStructure();
		} else
			Leaderboard.makeLeaderboards(); //Make the leaderboards

		for(Player player : getServer().getOnlinePlayers()){Users.addUser(player);} //In case of reload add all users back into PlayerProfile
		System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );  
		mcMMO_Timer.schedule(new mcTimer(this), (long)0, (long)(1000));
		//mcMMO_SpellTimer.schedule(new mcTimerSpells(this), (long)0, (long)(100));
	}

	public void setupPermissions() {
		Plugin test = this.getServer().getPluginManager().getPlugin("Permissions"); 
		if(this.PermissionsHandler == null) {
			if(test != null) {
				this.PermissionsHandler = ((Permissions)test).getHandler();
			} else {
				log.info("Messaging.bracketize(name) + Permission system not enabled. Disabling plugin."); 
				this.getServer().getPluginManager().disablePlugin(this);
			}
		}
	}

	public boolean inSameParty(Player playera, Player playerb){
		if(Users.getProfile(playera).inParty() && Users.getProfile(playerb).inParty()){
			if(Users.getProfile(playera).getParty().equals(Users.getProfile(playerb).getParty())){
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	public void getXpToLevel(Player player, String skillname){
		Users.getProfile(player).getXpToLevel(skillname.toLowerCase());
	}
	public void removeXp(Player player, String skillname, Integer newvalue){
		PlayerProfile PP = Users.getProfile(player);
		PP.removeXP(skillname, newvalue);
		Skills.XpCheck(player);
	}
	public void addXp(Player player, String skillname, Integer newvalue){
		PlayerProfile PP = Users.getProfile(player);
		PP.addXP(skillname, newvalue);
		Skills.XpCheck(player);
	}
	public void modifySkill(Player player, String skillname, Integer newvalue){
		PlayerProfile PP = Users.getProfile(player);
		PP.modifyskill(newvalue, skillname);
	}
	public ArrayList<String> getParties(){
		String location = "plugins/mcMMO/mcmmo.users"; 
		ArrayList<String> parties = new ArrayList<String>();
		try {
			//Open the users file
			FileReader file = new FileReader(location);
			BufferedReader in = new BufferedReader(file);
			String line = ""; 
			while((line = in.readLine()) != null)
			{
				String[] character = line.split(":"); 
				String theparty = null;
				//Party
				if(character.length > 3)
					theparty = character[3];
				if(!parties.contains(theparty))
					parties.add(theparty);
			}
			in.close();
		} catch (Exception e) {
			log.log(Level.SEVERE, "Exception while reading " 
					+ location + " (Are you sure you formatted it correctly?)", e); 
		}
		return parties;
	}
	public static String getPartyName(Player player){
		PlayerProfile PP = Users.getProfile(player);
		return PP.getParty();
	}
	public static boolean inParty(Player player){
		PlayerProfile PP = Users.getProfile(player);
		return PP.inParty();
	}
	public Permissions getPermissions() {
		return permissions;
	}
	public void onDisable() {
		System.out.println("mcMMO was disabled."); 
	}

	public boolean onCommand( CommandSender sender, Command command, String label, String[] args ) {
		long before = System.currentTimeMillis();
		Player player = (Player) sender;
		PlayerProfile PP = Users.getProfile(player);
		
		String[] split = new String[args.length + 1];
		split[0] = label;
		for(int a = 0; a < args.length; a++){
			split[a + 1] = args[a];
		}
		
		//Check if the command is an MMO related help command
		if(split[0].equalsIgnoreCase("taming") || split[0].toLowerCase().equalsIgnoreCase(Messages.getString("m.SkillTaming").toLowerCase())){ 
			float skillvalue = (float)PP.getSkill("taming");

			String percentage = String.valueOf((skillvalue / 1000) * 100);
			player.sendMessage(Messages.getString("m.SkillHeader", new Object[] {Messages.getString("m.SkillTaming")})); 
			player.sendMessage(Messages.getString("m.XPGain", new Object[] {Messages.getString("m.XPGainTaming")})); 
			if(mcPermissions.getInstance().taming(player))
				player.sendMessage(Messages.getString("m.LVL", new Object[] {PP.getSkillToString("taming"), PP.getSkillToString("tamingXP"), PP.getXpToLevel("taming")}));
			player.sendMessage(Messages.getString("m.SkillHeader", new Object[] {Messages.getString("m.Effects")})); 
			player.sendMessage(Messages.getString("m.EffectsTemplate", new Object[] {Messages.getString("m.EffectsTaming1_0"), Messages.getString("m.EffectsTaming1_1")}));  
			player.sendMessage(Messages.getString("m.EffectsTemplate", new Object[] {Messages.getString("m.EffectsTaming2_0"), Messages.getString("m.EffectsTaming2_1")}));  
			player.sendMessage(Messages.getString("m.EffectsTemplate", new Object[] {Messages.getString("m.EffectsTaming3_0"), Messages.getString("m.EffectsTaming3_1")}));  
			player.sendMessage(Messages.getString("m.EffectsTemplate", new Object[] {Messages.getString("m.EffectsTaming4_0"), Messages.getString("m.EffectsTaming4_1")}));  
			player.sendMessage(Messages.getString("m.EffectsTemplate", new Object[] {Messages.getString("m.EffectsTaming5_0"), Messages.getString("m.EffectsTaming5_1")}));  
			player.sendMessage(Messages.getString("m.EffectsTemplate", new Object[] {Messages.getString("m.EffectsTaming6_0"), Messages.getString("m.EffectsTaming6_1")}));  
			player.sendMessage(Messages.getString("m.SkillHeader", new Object[] {Messages.getString("m.YourStats")})); 
			if(PP.getSkill("taming") < 100)
				player.sendMessage(Messages.getString("m.AbilityLockTemplate", new Object[] {Messages.getString("m.AbilLockTaming1")})); 
			else
				player.sendMessage(Messages.getString("m.AbilityBonusTemplate", new Object[] {Messages.getString("m.AbilBonusTaming1_0"), Messages.getString("m.AbilBonusTaming1_1")}));  
			if(PP.getSkill("taming") < 250)
				player.sendMessage(Messages.getString("m.AbilityLockTemplate", new Object[] {Messages.getString("m.AbilLockTaming2")})); 
			else
				player.sendMessage(Messages.getString("m.AbilityBonusTemplate", new Object[] {Messages.getString("m.AbilBonusTaming2_0"), Messages.getString("m.AbilBonusTaming2_1")}));  
			if(PP.getSkill("taming") < 500)
				player.sendMessage(Messages.getString("m.AbilityLockTemplate", new Object[] {Messages.getString("m.AbilLockTaming3")})); 
			else
				player.sendMessage(Messages.getString("m.AbilityBonusTemplate", new Object[] {Messages.getString("m.AbilBonusTaming3_0"), Messages.getString("m.AbilBonusTaming3_1")}));  
			if(PP.getSkill("taming") < 750)
				player.sendMessage(Messages.getString("m.AbilityLockTemplate", new Object[] {Messages.getString("m.AbilLockTaming4")})); 
			else
				player.sendMessage(Messages.getString("m.AbilityBonusTemplate", new Object[] {Messages.getString("m.AbilBonusTaming4_0"), Messages.getString("m.AbilBonusTaming4_1")}));  
			player.sendMessage(Messages.getString("m.TamingGoreChance", new Object[] {percentage})); 
		}
		if(split[0].equalsIgnoreCase("woodcutting") || split[0].toLowerCase().equalsIgnoreCase(Messages.getString("m.SkillWoodCutting").toLowerCase())){ 
			float skillvalue = (float)PP.getSkill("woodcutting");
			int ticks = 2;
			int x = PP.getSkill("woodcutting");
			while(x >= 50){
				x-=50;
				ticks++;
			}
			String percentage = String.valueOf((skillvalue / 1000) * 100);
			player.sendMessage(Messages.getString("m.SkillHeader", new Object[] {Messages.getString("m.SkillWoodCutting")})); 
			player.sendMessage(Messages.getString("m.XPGain", new Object[] {Messages.getString("m.XPGainWoodCutting")})); 
			if(mcPermissions.getInstance().woodcutting(player))
				player.sendMessage(Messages.getString("m.LVL", new Object[] {PP.getSkillToString("woodcutting"), PP.getSkillToString("woodcuttingXP"), PP.getXpToLevel("woodcutting")}));
			player.sendMessage(Messages.getString("m.SkillHeader", new Object[] {Messages.getString("m.Effects")})); 
			player.sendMessage(Messages.getString("m.EffectsTemplate", new Object[] {Messages.getString("m.EffectsWoodCutting1_0"), Messages.getString("m.EffectsWoodCutting1_1")}));  
			player.sendMessage(Messages.getString("m.EffectsTemplate", new Object[] {Messages.getString("m.EffectsWoodCutting2_0"), Messages.getString("m.EffectsWoodCutting2_1")}));  
			player.sendMessage(Messages.getString("m.EffectsTemplate", new Object[] {Messages.getString("m.EffectsWoodCutting3_0"), Messages.getString("m.EffectsWoodCutting3_1")}));  
			player.sendMessage(Messages.getString("m.SkillHeader", new Object[] {Messages.getString("m.YourStats")})); 
			if(PP.getSkill("woodcutting") < 100)
				player.sendMessage(Messages.getString("m.AbilityLockTemplate", new Object[] {Messages.getString("m.AbilLockWoodCutting1")})); 
			else
				player.sendMessage(Messages.getString("m.AbilityBonusTemplate", new Object[] {Messages.getString("m.AbilBonusWoodCutting1_0"), Messages.getString("m.AbilBonusWoodCutting1_1")}));  
			player.sendMessage(Messages.getString("m.WoodCuttingDoubleDropChance", new Object[] {percentage})); 
			player.sendMessage(Messages.getString("m.WoodCuttingTreeFellerLength", new Object[] {ticks})); 
		}
		if(split[0].equalsIgnoreCase("archery") || split[0].toLowerCase().equalsIgnoreCase(Messages.getString("m.SkillArchery").toLowerCase())){ 
			Integer rank = 0;
			if(PP.getSkill("archery") >= 50)
				rank++;
			if(PP.getSkill("archery") >= 250)
				rank++;
			if(PP.getSkill("archery") >= 575)
				rank++;
			if(PP.getSkill("archery") >= 725)
				rank++;
			if(PP.getSkill("archery") >= 1000)
				rank++;
			float skillvalue = (float)PP.getSkill("archery");
			String percentage = String.valueOf((skillvalue / 1000) * 100);

			int ignition = 20;
			if(PP.getSkill("archery") >= 200)
				ignition+=20;
			if(PP.getSkill("archery") >= 400)
				ignition+=20;
			if(PP.getSkill("archery") >= 600)
				ignition+=20;
			if(PP.getSkill("archery") >= 800)
				ignition+=20;
			if(PP.getSkill("archery") >= 1000)
				ignition+=20;

			String percentagedaze;
			if(PP.getSkill("archery") < 1000){
				percentagedaze = String.valueOf((skillvalue / 2000) * 100);
			} else {
				percentagedaze = "50"; 
			}
			player.sendMessage(Messages.getString("m.SkillHeader", new Object[] {Messages.getString("m.SkillArchery")})); 
			player.sendMessage(Messages.getString("m.XPGain", new Object[] {Messages.getString("m.XPGainArchery")})); 
			if(mcPermissions.getInstance().archery(player))
				player.sendMessage(Messages.getString("m.LVL", new Object[] {PP.getSkillToString("archery"), PP.getSkillToString("archeryXP"), PP.getXpToLevel("archery")}));
			player.sendMessage(Messages.getString("m.SkillHeader", new Object[] {Messages.getString("m.Effects")})); 
			player.sendMessage(Messages.getString("m.EffectsTemplate", new Object[] {Messages.getString("m.EffectsArchery1_0"), Messages.getString("m.EffectsArchery1_1")}));  
			player.sendMessage(Messages.getString("m.EffectsTemplate", new Object[] {Messages.getString("m.EffectsArchery2_0"), Messages.getString("m.EffectsArchery2_1")}));  
			player.sendMessage(Messages.getString("m.EffectsTemplate", new Object[] {Messages.getString("m.EffectsArchery3_0"), Messages.getString("m.EffectsArchery3_1")}));  
			player.sendMessage(Messages.getString("m.EffectsTemplate", new Object[] {Messages.getString("m.EffectsArchery4_0"), Messages.getString("m.EffectsArchery4_1")}));  
			player.sendMessage(Messages.getString("m.SkillHeader", new Object[] {Messages.getString("m.YourStats")})); 
			player.sendMessage(Messages.getString("m.ArcheryDazeChance", new Object[] {percentagedaze})); 
			player.sendMessage(Messages.getString("m.ArcheryRetrieveChance", new Object[] {percentage})); 
			player.sendMessage(Messages.getString("m.ArcheryIgnitionLength", new Object[] {(ignition / 20)})); 
			player.sendMessage(Messages.getString("m.ArcheryDamagePlus", new Object[] {rank})); 
		}
		if(split[0].equalsIgnoreCase("axes") || split[0].toLowerCase().equalsIgnoreCase(Messages.getString("m.SkillAxes"))){ 
			String percentage;
			float skillvalue = (float)PP.getSkill("axes");
			if(PP.getSkill("axes") < 750){
				percentage = String.valueOf((skillvalue / 1000) * 100);
			} else {
				percentage = "75"; 
			}
			int ticks = 2;
			int x = PP.getSkill("axes");
			while(x >= 50){
				x-=50;
				ticks++;
			}

			player.sendMessage(Messages.getString("m.SkillHeader", new Object[] {Messages.getString("m.SkillAxes")})); 
			player.sendMessage(Messages.getString("m.XPGain", new Object[] {Messages.getString("m.XPGainAxes")})); 
			if(mcPermissions.getInstance().axes(player))
				player.sendMessage(Messages.getString("m.LVL", new Object[] {PP.getSkillToString("axes"), PP.getSkillToString("axesXP"), PP.getXpToLevel("axes")}));
			player.sendMessage(Messages.getString("m.SkillHeader", new Object[] {Messages.getString("m.Effects")})); 
			player.sendMessage(Messages.getString("m.EffectsTemplate", new Object[] {Messages.getString("m.EffectsAxes1_0"), Messages.getString("m.EffectsAxes1_1")}));  
			player.sendMessage(Messages.getString("m.EffectsTemplate", new Object[] {Messages.getString("m.EffectsAxes2_0"), Messages.getString("m.EffectsAxes2_1")}));  
			player.sendMessage(Messages.getString("m.EffectsTemplate", new Object[] {Messages.getString("m.EffectsAxes3_0"), Messages.getString("m.EffectsAxes3_1")}));  
			player.sendMessage(Messages.getString("m.SkillHeader", new Object[] {Messages.getString("m.YourStats")})); 
			player.sendMessage(Messages.getString("m.AxesCritChance", new Object[] {percentage})); 
			if(PP.getSkill("axes") < 500){
				player.sendMessage(Messages.getString("m.AbilityLockTemplate", new Object[] {Messages.getString("m.AbilLockAxes1")})); 
			} else {
				player.sendMessage(Messages.getString("m.AbilityBonusTemplate", new Object[] {Messages.getString("m.AbilBonusAxes1_0"), Messages.getString("m.AbilBonusAxes1_1")}));  
			}
			player.sendMessage(Messages.getString("m.AxesSkullLength", new Object[] {ticks})); 
		}
		if(split[0].equalsIgnoreCase("swords") || split[0].toLowerCase().equalsIgnoreCase(Messages.getString("m.SkillSwords").toLowerCase())){ 
			int bleedrank = 2;
			String percentage, parrypercentage = null, counterattackpercentage;
			float skillvalue = (float)PP.getSkill("swords");
			if(PP.getSkill("swords") < 750){
				percentage = String.valueOf((skillvalue / 1000) * 100);
			} else {
				percentage = "75"; 
			}
			if(skillvalue >= 750)
				bleedrank+=1;

			if(PP.getSkill("swords") <= 900){
				parrypercentage = String.valueOf((skillvalue / 3000) * 100);
			} else {
				parrypercentage = "30"; 
			}

			if(PP.getSkill("swords") <= 600){
				counterattackpercentage = String.valueOf((skillvalue / 2000) * 100);
			} else {
				counterattackpercentage = "30"; 
			}

			int ticks = 2;
			int x = PP.getSkill("swords");
			while(x >= 50){
				x-=50;
				ticks++;
			}

			player.sendMessage(Messages.getString("m.SkillHeader", new Object[] {Messages.getString("m.SkillSwords")})); 
			player.sendMessage(Messages.getString("m.XPGain", new Object[] {Messages.getString("m.XPGainSwords")})); 
			if(mcPermissions.getInstance().swords(player))
				player.sendMessage(Messages.getString("m.LVL", new Object[] {PP.getSkillToString("swords"), PP.getSkillToString("swordsXP"), PP.getXpToLevel("swords")}));
			player.sendMessage(Messages.getString("m.SkillHeader", new Object[] {Messages.getString("m.Effects")})); 
			player.sendMessage(Messages.getString("m.EffectsTemplate", new Object[] {Messages.getString("m.EffectsSwords1_0"), Messages.getString("m.EffectsSwords1_1")}));  
			player.sendMessage(Messages.getString("m.EffectsTemplate", new Object[] {Messages.getString("m.EffectsSwords2_0"), Messages.getString("m.EffectsSwords2_1")}));  
			player.sendMessage(Messages.getString("m.EffectsTemplate", new Object[] {Messages.getString("m.EffectsSwords3_0"), Messages.getString("m.EffectsSwords3_1")}));  
			player.sendMessage(Messages.getString("m.EffectsTemplate", new Object[] {Messages.getString("m.EffectsSwords4_0"), Messages.getString("m.EffectsSwords4_1")}));  
			player.sendMessage(Messages.getString("m.EffectsTemplate", new Object[] {Messages.getString("m.EffectsSwords5_0"), Messages.getString("m.EffectsSwords5_1")}));  
			player.sendMessage(Messages.getString("m.SkillHeader", new Object[] {Messages.getString("m.YourStats")})); 
			player.sendMessage(Messages.getString("m.SwordsCounterAttChance", new Object[] {counterattackpercentage})); 
			player.sendMessage(Messages.getString("m.SwordsBleedLength", new Object[] {bleedrank})); 
			player.sendMessage(Messages.getString("m.SwordsTickNote")); 
			player.sendMessage(Messages.getString("m.SwordsBleedLength", new Object[] {percentage})); 
			player.sendMessage(Messages.getString("m.SwordsParryChance", new Object[] {parrypercentage})); 
			player.sendMessage(Messages.getString("m.SwordsSSLength", new Object[] {ticks})); 

		}
		if(split[0].equalsIgnoreCase("acrobatics") || split[0].toLowerCase().equalsIgnoreCase(Messages.getString("m.SkillAcrobatics").toLowerCase())){ 
			String dodgepercentage;
			float skillvalue = (float)PP.getSkill("acrobatics");
			String percentage = String.valueOf((skillvalue / 1000) * 100);
			String gracepercentage = String.valueOf(((skillvalue / 1000) * 100) * 2);
			if(PP.getSkill("acrobatics") <= 800){
				dodgepercentage = String.valueOf((skillvalue / 4000 * 100));
			} else {
				dodgepercentage = "20"; 
			}
			player.sendMessage(Messages.getString("m.SkillHeader", new Object[] {Messages.getString("m.SkillAcrobatics")})); 
			player.sendMessage(Messages.getString("m.XPGain", new Object[] {Messages.getString("m.XPGainAcrobatics")})); 
			if(mcPermissions.getInstance().acrobatics(player))
				player.sendMessage(Messages.getString("m.LVL", new Object[] {PP.getSkillToString("acrobatics"), PP.getSkillToString("acrobaticsXP"), PP.getXpToLevel("acrobatics")}));
			player.sendMessage(Messages.getString("m.SkillHeader", new Object[] {Messages.getString("m.Effects")})); 
			player.sendMessage(Messages.getString("m.EffectsTemplate", new Object[] {Messages.getString("m.EffectsAcrobatics1_0"), Messages.getString("m.EffectsAcrobatics1_1")}));  
			player.sendMessage(Messages.getString("m.EffectsTemplate", new Object[] {Messages.getString("m.EffectsAcrobatics2_0"), Messages.getString("m.EffectsAcrobatics2_1")}));  
			player.sendMessage(Messages.getString("m.EffectsTemplate", new Object[] {Messages.getString("m.EffectsAcrobatics3_0"), Messages.getString("m.EffectsAcrobatics3_1")}));  
			player.sendMessage(Messages.getString("m.SkillHeader", new Object[] {Messages.getString("m.YourStats")})); 
			player.sendMessage(Messages.getString("m.AcrobaticsRollChance", new Object[] {percentage})); 
			player.sendMessage(Messages.getString("m.AcrobaticsGracefulRollChance", new Object[] {gracepercentage})); 
			player.sendMessage(Messages.getString("m.AcrobaticsDodgeChance", new Object[] {dodgepercentage})); 
		}
		if(split[0].equalsIgnoreCase("mining") || split[0].toLowerCase().equalsIgnoreCase(Messages.getString("m.SkillMining"))){ 
			float skillvalue = (float)PP.getSkill("mining");
			String percentage = String.valueOf((skillvalue / 1000) * 100);
			int ticks = 2;
			int x = PP.getSkill("mining");
			while(x >= 50){
				x-=50;
				ticks++;
			}
			player.sendMessage(Messages.getString("m.SkillHeader", new Object[] {Messages.getString("m.SkillMining")})); 
			player.sendMessage(Messages.getString("m.XPGain", new Object[] {Messages.getString("m.XPGainMining")})); 
			if(mcPermissions.getInstance().mining(player))
				player.sendMessage(Messages.getString("m.LVL", new Object[] {PP.getSkillToString("mining"), PP.getSkillToString("miningXP"), PP.getXpToLevel("mining")}));
			player.sendMessage(Messages.getString("m.SkillHeader", new Object[] {Messages.getString("m.Effects")})); 
			player.sendMessage(Messages.getString("m.EffectsTemplate", new Object[] {Messages.getString("m.EffectsMining1_0"), Messages.getString("m.EffectsMining1_1")}));  
			player.sendMessage(Messages.getString("m.EffectsTemplate", new Object[] {Messages.getString("m.EffectsMining2_0"), Messages.getString("m.EffectsMining2_1")}));  
			player.sendMessage(Messages.getString("m.SkillHeader", new Object[] {Messages.getString("m.YourStats")})); 
			player.sendMessage(Messages.getString("m.MiningDoubleDropChance", new Object[] {percentage})); 
			player.sendMessage(Messages.getString("m.MiningSuperBreakerLength", new Object[] {ticks})); 
		}
		if(split[0].equalsIgnoreCase("repair") || split[0].toLowerCase().equalsIgnoreCase(Messages.getString("m.SkillRepair").toLowerCase())){ 
			float skillvalue = (float)PP.getSkill("repair");
			String percentage = String.valueOf((skillvalue / 1000) * 100);
			String repairmastery = String.valueOf((skillvalue / 500) * 100);
			player.sendMessage(Messages.getString("m.SkillHeader", new Object[] {Messages.getString("m.SkillRepair")})); 
			player.sendMessage(Messages.getString("m.XPGain", new Object[] {Messages.getString("m.XPGainRepair")})); 
			if(mcPermissions.getInstance().repair(player))
				player.sendMessage(Messages.getString("m.LVL", new Object[] {PP.getSkillToString("repair"), PP.getSkillToString("repairXP"), PP.getXpToLevel("repair")}));
			player.sendMessage(Messages.getString("m.SkillHeader", new Object[] {Messages.getString("m.Effects")})); 
			player.sendMessage(Messages.getString("m.EffectsTemplate", new Object[] {Messages.getString("m.EffectsRepair1_0"), Messages.getString("m.EffectsRepair1_1")}));  
			player.sendMessage(Messages.getString("m.EffectsTemplate", new Object[] {Messages.getString("m.EffectsRepair2_0"), Messages.getString("m.EffectsRepair2_1")}));  
			player.sendMessage(Messages.getString("m.EffectsTemplate", new Object[] {Messages.getString("m.EffectsRepair3_0"), Messages.getString("m.EffectsRepair3_1")}));  
			player.sendMessage(Messages.getString("m.EffectsTemplate", new Object[] {Messages.getString("m.EffectsRepair4_0", new Object[]{LoadProperties.repairdiamondlevel}), Messages.getString("m.EffectsRepair4_1")}));  
			player.sendMessage(Messages.getString("m.SkillHeader", new Object[] {Messages.getString("m.YourStats")})); 
			player.sendMessage(Messages.getString("m.RepairRepairMastery", new Object[] {repairmastery})); 
			player.sendMessage(Messages.getString("m.RepairSuperRepairChance", new Object[] {percentage})); 
		}
		if(split[0].equalsIgnoreCase("unarmed")){ 
			String percentage, arrowpercentage;
			float skillvalue = (float)PP.getSkill("unarmed");

			if(PP.getSkill("unarmed") < 1000){
				percentage = String.valueOf((skillvalue / 4000) * 100);
			} else {
				percentage = "25"; 
			}

			if(PP.getSkill("unarmed") < 1000){
				arrowpercentage = String.valueOf(((skillvalue / 1000) * 100) / 2);
			} else {
				arrowpercentage = "50"; 
			}


			int ticks = 2;
			int x = PP.getSkill("unarmed");
			while(x >= 50){
				x-=50;
				ticks++;
			}

			player.sendMessage(Messages.getString("m.SkillHeader", new Object[] {Messages.getString("m.SkillUnarmed")})); 
			player.sendMessage(Messages.getString("m.XPGain", new Object[] {Messages.getString("m.XPGainUnarmed")})); 
			if(mcPermissions.getInstance().unarmed(player))
				player.sendMessage(Messages.getString("m.LVL", new Object[] {PP.getSkillToString("unarmed"), PP.getSkillToString("unarmedXP"), PP.getXpToLevel("unarmed")}));
			player.sendMessage(Messages.getString("m.SkillHeader", new Object[] {Messages.getString("m.Effects")})); 
			player.sendMessage(Messages.getString("m.EffectsTemplate", new Object[] {Messages.getString("m.EffectsUnarmed1_0"), Messages.getString("m.EffectsUnarmed1_1")}));  
			player.sendMessage(Messages.getString("m.EffectsTemplate", new Object[] {Messages.getString("m.EffectsUnarmed2_0"), Messages.getString("m.EffectsUnarmed2_1")}));  
			player.sendMessage(Messages.getString("m.EffectsTemplate", new Object[] {Messages.getString("m.EffectsUnarmed3_0"), Messages.getString("m.EffectsUnarmed3_1")}));  
			player.sendMessage(Messages.getString("m.EffectsTemplate", new Object[] {Messages.getString("m.EffectsUnarmed4_0"), Messages.getString("m.EffectsUnarmed4_1")}));  
			player.sendMessage(Messages.getString("m.EffectsTemplate", new Object[] {Messages.getString("m.EffectsUnarmed5_0"), Messages.getString("m.EffectsUnarmed5_1")}));  
			player.sendMessage(Messages.getString("m.SkillHeader", new Object[] {Messages.getString("m.YourStats")})); 
			player.sendMessage(Messages.getString("m.UnarmedArrowDeflectChance", new Object[] {arrowpercentage})); 
			player.sendMessage(Messages.getString("m.UnarmedDisarmChance", new Object[] {percentage})); 
			if(PP.getSkill("unarmed") < 250){
				player.sendMessage(Messages.getString("m.AbilityLockTemplate", new Object[] {Messages.getString("m.AbilLockUnarmed1")})); 
			} else if(PP.getSkill("unarmed") >= 250 && PP.getSkill("unarmed") < 500){
				player.sendMessage(Messages.getString("m.AbilityBonusTemplate", new Object[] {Messages.getString("m.AbilBonusUnarmed1_0"), Messages.getString("m.AbilBonusUnarmed1_1")}));  
				player.sendMessage(Messages.getString("m.AbilityLockTemplate", new Object[] {Messages.getString("m.AbilLockUnarmed2")})); 
			} else {
				player.sendMessage(Messages.getString("m.AbilityBonusTemplate", new Object[] {Messages.getString("m.AbilBonusUnarmed2_0"), Messages.getString("m.AbilBonusUnarmed2_1")}));  
			}
			player.sendMessage(Messages.getString("m.UnarmedBerserkLength", new Object[] {ticks})); 
		}
		if(split[0].equalsIgnoreCase("herbalism") || split[0].toLowerCase().equalsIgnoreCase(Messages.getString("m.SkillHerbalism").toLowerCase())){ 
			int rank = 0;
			if(PP.getSkill("herbalism") >= 50)
				rank++;
			if (PP.getSkill("herbalism") >= 150)
				rank++;
			if (PP.getSkill("herbalism") >= 250)
				rank++;
			if (PP.getSkill("herbalism") >= 350)
				rank++;
			if (PP.getSkill("herbalism") >= 450)
				rank++;
			if (PP.getSkill("herbalism") >= 550)
				rank++;
			if (PP.getSkill("herbalism") >= 650)
				rank++;
			if (PP.getSkill("herbalism") >= 750)
				rank++;
			int bonus = 0;
			if(PP.getSkill("herbalism") >= 200)
				bonus++;
			if(PP.getSkill("herbalism") >= 400)
				bonus++;
			if(PP.getSkill("herbalism") >= 600)
				bonus++;

			int ticks = 2;
			int x = PP.getSkill("herbalism");
			while(x >= 50){
				x-=50;
				ticks++;
			}

			float skillvalue = (float)PP.getSkill("herbalism");
			String percentage = String.valueOf((skillvalue / 1000) * 100);
			String gpercentage = String.valueOf((skillvalue / 1500) * 100);
			player.sendMessage(Messages.getString("m.SkillHeader", new Object[] {Messages.getString("m.SkillHerbalism")})); 
			player.sendMessage(Messages.getString("m.XPGain", new Object[] {Messages.getString("m.XPGainHerbalism")})); 
			if(mcPermissions.getInstance().herbalism(player))
				player.sendMessage(Messages.getString("m.LVL", new Object[] {PP.getSkillToString("herbalism"), PP.getSkillToString("herbalismXP"), PP.getXpToLevel("herbalism")}));
			player.sendMessage(Messages.getString("m.SkillHeader", new Object[] {Messages.getString("m.Effects")})); 
			player.sendMessage(Messages.getString("m.EffectsTemplate", new Object[] {Messages.getString("m.EffectsHerbalism1_0"), Messages.getString("m.EffectsHerbalism1_1")}));  
			player.sendMessage(Messages.getString("m.EffectsTemplate", new Object[] {Messages.getString("m.EffectsHerbalism2_0"), Messages.getString("m.EffectsHerbalism2_1")}));  
			player.sendMessage(Messages.getString("m.EffectsTemplate", new Object[] {Messages.getString("m.EffectsHerbalism3_0"), Messages.getString("m.EffectsHerbalism3_1")}));  
			player.sendMessage(Messages.getString("m.EffectsTemplate", new Object[] {Messages.getString("m.EffectsHerbalism4_0"), Messages.getString("m.EffectsHerbalism4_1")}));  
			player.sendMessage(Messages.getString("m.EffectsTemplate", new Object[] {Messages.getString("m.EffectsHerbalism5_0"), Messages.getString("m.EffectsHerbalism5_1")}));  
			player.sendMessage(Messages.getString("m.SkillHeader", new Object[] {Messages.getString("m.YourStats")})); 
			player.sendMessage(Messages.getString("m.HerbalismGreenTerraLength", new Object[] {ticks})); 
			player.sendMessage(Messages.getString("m.HerbalismGreenThumbChance", new Object[] {gpercentage})); 
			player.sendMessage(Messages.getString("m.HerbalismGreenThumbStage", new Object[] {bonus})); 
			player.sendMessage(Messages.getString("m.HerbalismDoubleDropChance", new Object[] {percentage})); 
			player.sendMessage(Messages.getString("m.HerbalismFoodPlus", new Object[] {rank})); 
		}

		if(split[0].equalsIgnoreCase("excavation") || split[0].toLowerCase().equalsIgnoreCase(Messages.getString("m.SkillExcavation").toLowerCase())) 
		{
			int ticks = 2;
			int x = PP.getSkill("excavation");
			while(x >= 50){
				x-=50;
				ticks++;
			}
			player.sendMessage(Messages.getString("m.SkillHeader", new Object[] {Messages.getString("m.SkillExcavation")})); 
			player.sendMessage(Messages.getString("m.XPGain", new Object[] {Messages.getString("m.XPGainExcavation")})); 
			if(mcPermissions.getInstance().excavation(player))
				player.sendMessage(Messages.getString("m.LVL", new Object[] {PP.getSkillToString("excavation"), PP.getSkillToString("excavationXP"), PP.getXpToLevel("excavation")}));
			player.sendMessage(Messages.getString("m.SkillHeader", new Object[] {Messages.getString("m.Effects")})); 
			player.sendMessage(Messages.getString("m.EffectsTemplate", new Object[] {Messages.getString("m.EffectsExcavation1_0"), Messages.getString("m.EffectsExcavation1_1")}));  
			player.sendMessage(Messages.getString("m.EffectsTemplate", new Object[] {Messages.getString("m.EffectsExcavation2_0"), Messages.getString("m.EffectsExcavation2_1")}));  
			player.sendMessage(Messages.getString("m.SkillHeader", new Object[] {Messages.getString("m.YourStats")})); 
			player.sendMessage(Messages.getString("m.ExcavationGreenTerraLength", new Object[] {ticks})); 
		}

		if(split[0].equalsIgnoreCase("sorcery") || split[0].toLowerCase().equalsIgnoreCase(Messages.getString("m.SkillSorcery").toLowerCase())) 
		{

			/*
	        player.sendMessage(Messages.getString("m.SkillHeader", new Object[] {Messages.getString("m.SkillExcavation")})); 
			player.sendMessage(Messages.getString("m.XPGain", new Object[] {Messages.getString("m.XPGainExcavation")})); 
			player.sendMessage(Messages.getString("m.SkillHeader", new Object[] {Messages.getString("m.Effects")})); 
			player.sendMessage(Messages.getString("m.EffectsTemplate", new Object[] {Messages.getString("m.EffectsExcavation1_0"), Messages.getString("m.EffectsExcavation1_1")}));  
			player.sendMessage(Messages.getString("m.EffectsTemplate", new Object[] {Messages.getString("m.EffectsExcavation2_0"), Messages.getString("m.EffectsExcavation2_1")}));  
			player.sendMessage(Messages.getString("m.SkillHeader", new Object[] {Messages.getString("m.YourStats")})); 
			player.sendMessage(Messages.getString("m.ExcavationGreenTerraLength", new Object[] {ticks})); 
			 */
		}

		if(LoadProperties.mcmmoEnable && split[0].equalsIgnoreCase(LoadProperties.mcmmo)){ 
			player.sendMessage(ChatColor.RED+"-----[]"+ChatColor.GREEN+"mMO"+ChatColor.RED+"[]-----");   
			player.sendMessage(ChatColor.YELLOW+"mcMMO is an RPG server mod for minecraft."); 
			player.sendMessage(ChatColor.YELLOW+"There are many skills added by mcMMO to minecraft."); 
			player.sendMessage(ChatColor.YELLOW+"They can do anything from giving a chance"); 
			player.sendMessage(ChatColor.YELLOW+"for double drops to letting you break materials instantly."); 
			player.sendMessage(ChatColor.YELLOW+"For example, by harvesting logs from trees you will gain"); 
			player.sendMessage(ChatColor.YELLOW+"Woodcutting xp and once you have enough xp you will gain"); 
			player.sendMessage(ChatColor.YELLOW+"a skill level in Woodcutting. By raising this skill you will"); 
			player.sendMessage(ChatColor.YELLOW+"be able to receive benefits like "+ChatColor.RED+"double drops");  
			player.sendMessage(ChatColor.YELLOW+"and increase the effects of the "+ChatColor.RED+"\"Tree Felling\""+ChatColor.YELLOW+" ability.");   
			player.sendMessage(ChatColor.YELLOW+"mMO has abilities related to the skill, skills normally"); 
			player.sendMessage(ChatColor.YELLOW+"provide passive bonuses but they also have activated"); 
			player.sendMessage(ChatColor.YELLOW+"abilities too. Each ability is activated by holding"); 
			player.sendMessage(ChatColor.YELLOW+"the appropriate tool and "+ChatColor.RED+"right clicking.");  
			player.sendMessage(ChatColor.YELLOW+"For example, if you hold a Mining Pick and right click"); 
			player.sendMessage(ChatColor.YELLOW+"you will ready your Pickaxe, attack mining materials"); 
			player.sendMessage(ChatColor.YELLOW+"and then "+ChatColor.RED+"Super Breaker "+ChatColor.YELLOW+"will activate.");   
			player.sendMessage(ChatColor.GREEN+"Find out mcMMO commands with "+ChatColor.DARK_AQUA+LoadProperties.mcc);  
			player.sendMessage(ChatColor.GREEN+"You can donate via paypal to"+ChatColor.DARK_RED+" nossr50@gmail.com");  
		}
		if(LoadProperties.mccEnable && split[0].equalsIgnoreCase(LoadProperties.mcc)){ 
			player.sendMessage(ChatColor.RED+"---[]"+ChatColor.YELLOW+"mcMMO Commands"+ChatColor.RED+"[]---");   
			if(mcPermissions.getInstance().party(player)){
				player.sendMessage(Messages.getString("m.mccPartyCommands")); 
				player.sendMessage(LoadProperties.party+" "+Messages.getString("m.mccParty"));   
				player.sendMessage(LoadProperties.party+" q "+Messages.getString("m.mccPartyQ"));
				if(mcPermissions.getInstance().partyChat(player))
					player.sendMessage("/p "+Messages.getString("m.mccPartyToggle"));  
				player.sendMessage(LoadProperties.invite+" "+Messages.getString("m.mccPartyInvite"));   
				player.sendMessage(LoadProperties.accept+" "+Messages.getString("m.mccPartyAccept"));   
				if(mcPermissions.getInstance().partyTeleport(player))
					player.sendMessage(LoadProperties.ptp+" "+Messages.getString("m.mccPartyTeleport"));   
			}
			player.sendMessage(Messages.getString("m.mccOtherCommands")); 
			player.sendMessage(LoadProperties.stats+ChatColor.RED+" "+Messages.getString("m.mccStats"));  
			player.sendMessage("/mctop <skillname> <page> "+ChatColor.RED+Messages.getString("m.mccLeaderboards"));  
			if(mcPermissions.getInstance().mySpawn(player)){
				player.sendMessage(LoadProperties.myspawn+" "+ChatColor.RED+Messages.getString("m.mccMySpawn"));   
				player.sendMessage(LoadProperties.clearmyspawn+" "+ChatColor.RED+Messages.getString("m.mccClearMySpawn"));   
			}
			if(mcPermissions.getInstance().mcAbility(player))
				player.sendMessage(LoadProperties.mcability+ChatColor.RED+" "+Messages.getString("m.mccToggleAbility"));  
			if(mcPermissions.getInstance().adminChat(player)){
				player.sendMessage("/a "+ChatColor.RED+Messages.getString("m.mccAdminToggle"));  
			}
			if(mcPermissions.getInstance().whois(player))
				player.sendMessage(LoadProperties.whois+" "+Messages.getString("m.mccWhois"));   
			if(mcPermissions.getInstance().mmoedit(player)){
				//player.sendMessage(LoadProperties.mmoedit+" [skill] [newvalue] "+ChatColor.RED+"Modify the designated skill value");
				player.sendMessage(LoadProperties.mmoedit+Messages.getString("m.mccMmoedit"));   
			}
			if(mcPermissions.getInstance().mcgod(player))
				player.sendMessage(LoadProperties.mcgod+ChatColor.RED+" "+Messages.getString("m.mccMcGod"));  
			player.sendMessage(Messages.getString("m.mccSkillInfo"));  
			player.sendMessage(LoadProperties.mcmmo+" "+Messages.getString("m.mccModDescription"));   
		}
		if(LoadProperties.mcabilityEnable && mcPermissions.permissionsEnabled && split[0].equalsIgnoreCase(LoadProperties.mcability)){ 
			if(PP.getAbilityUse()){
				player.sendMessage(Messages.getString("mcPlayerListener.AbilitiesOff")); 
				PP.toggleAbilityUse();
			} else {
				player.sendMessage(Messages.getString("mcPlayerListener.AbilitiesOn")); 
				PP.toggleAbilityUse();
			}
		}

		/*
    	if(split[0].equalsIgnoreCase("/mmosupplies") && player.isOp())
    	{
    		Location location = event.getPlayer().getLocation();
    		World world = location.getWorld();
    		world.spawnCreature(location, CreatureType.WOLF);

    		ItemStack[] dye = new ItemStack[15];
    		int y = 0;
    		for(ItemStack x : dye)
    		{
    			dye[y] = new ItemStack(351, 64, (short) y);
    			event.getPlayer().getWorld().dropItemNaturally(event.getPlayer().getLocation(), dye[y]);
    			y++;
    		}
    	}
		 */

		/*
		 * FFS -> MySQL
		 */
		if(split[0].equalsIgnoreCase("mmoupdate")){
			if(!mcPermissions.getInstance().admin(player)){
				player.sendMessage(ChatColor.YELLOW+"[mcMMO]"+ChatColor.DARK_RED +Messages.getString("mcPlayerListener.NoPermission"));  
				return true;
			}
			player.sendMessage(ChatColor.GRAY+"Starting conversion..."); 
			Users.clearUsers();
			m.convertToMySQL(this);
			for(Player x : this.getServer().getOnlinePlayers())
			{
				Users.addUser(x);
			}
			player.sendMessage(ChatColor.GREEN+"Conversion finished!"); 
		}

		/*
		 * LEADER BOARD COMMAND
		 */
		if(LoadProperties.mctopEnable && split[0].equalsIgnoreCase(LoadProperties.mctop)){ 

			if(LoadProperties.useMySQL == false){
				/*
				 * POWER LEVEL INFO RETRIEVAL
				 */
				if(split.length == 1){
					int p = 1;
					String[] info = Leaderboard.retrieveInfo("powerlevel", p); 
					player.sendMessage(Messages.getString("mcPlayerListener.PowerLevelLeaderboard"));
					int n = 1 * p; //Position
					for(String x : info){
						if(x != null){
							String digit = String.valueOf(n);
							if(n < 10)
								digit ="0"+String.valueOf(n); 
							String[] splitx = x.split(":"); 
							//Format: 1. Playername - skill value
							player.sendMessage(digit+". "+ChatColor.GREEN+splitx[1]+" - "+ChatColor.WHITE+splitx[0]);  
							n++;
						}
					}
				}
				if(split.length >= 2 && Leaderboard.isInt(split[1])){
					int p = 1;
					//Grab page value if specified
					if(split.length >= 2){
						if(Leaderboard.isInt(split[1])){
							p = Integer.valueOf(split[1]);
						}
					}
					int pt = p;
					if(p > 1){
						pt -= 1;
						pt += (pt * 10);
						pt = 10;
					}
					String[] info = Leaderboard.retrieveInfo("powerlevel", p); 
					player.sendMessage(Messages.getString("mcPlayerListener.PowerLevelLeaderboard")); 
					int n = 1 * pt; //Position
					for(String x : info){
						if(x != null){
							String digit = String.valueOf(n);
							if(n < 10)
								digit ="0"+String.valueOf(n); 
							String[] splitx = x.split(":"); 
							//Format: 1. Playername - skill value
							player.sendMessage(digit+". "+ChatColor.GREEN+splitx[1]+" - "+ChatColor.WHITE+splitx[0]);  
							n++;
						}
					}
				}
				/*
				 * SKILL SPECIFIED INFO RETRIEVAL
				 */
				if(split.length >= 2 && Skills.isSkill(split[1])){
					int p = 1;
					//Grab page value if specified
					if(split.length >= 3){
						if(Leaderboard.isInt(split[2])){
							p = Integer.valueOf(split[2]);
						}
					}
					int pt = p;
					if(p > 1){
						pt -= 1;
						pt += (pt * 10);
						pt = 10;
					}
					String firstLetter = split[1].substring(0,1);  // Get first letter
					String remainder   = split[1].substring(1);    // Get remainder of word.
					String capitalized = firstLetter.toUpperCase() + remainder.toLowerCase();

					String[] info = Leaderboard.retrieveInfo(split[1].toLowerCase(), p);
					player.sendMessage(Messages.getString("mcPlayerListener.SkillLeaderboard", new Object[] {capitalized}));  
					int n = 1 * pt; //Position
					for(String x : info){
						if(x != null){
							String digit = String.valueOf(n);
							if(n < 10)
								digit ="0"+String.valueOf(n); 
							String[] splitx = x.split(":"); 
							//Format: 1. Playername - skill value
							player.sendMessage(digit+". "+ChatColor.GREEN+splitx[1]+" - "+ChatColor.WHITE+splitx[0]);  
							n++;
						}
					}
				}
			} else {
				/*
				 * MYSQL LEADERBOARDS
				 */
				String powerlevel = "taming+mining+woodcutting+repair+unarmed+herbalism+excavation+archery+swords+axes+acrobatics"; 
				if(split.length >= 2 && Skills.isSkill(split[1])){
					/*
					 * Create a nice consistent capitalized leaderboard name
					 */
					String lowercase = split[1].toLowerCase(); //For the query
					String firstLetter = split[1].substring(0,1); //Get first letter
					String remainder   = split[1].substring(1); //Get remainder of word.
					String capitalized = firstLetter.toUpperCase() + remainder.toLowerCase();

					player.sendMessage(Messages.getString("mcPlayerListener.SkillLeaderboard", new Object[] {capitalized}));  
					if(split.length >= 3 && m.isInt(split[2])){
						int n = 1; //For the page number
						int n2 = Integer.valueOf(split[2]);
						if(n2 > 1){
							//Figure out the 'page' here
							n = 10;
							n = n * (n2-1);
						}
						//If a page number is specified
						HashMap<Integer, ArrayList<String>> userslist = mcMMO.database.Read("SELECT "+lowercase+", user_id FROM "  
								+LoadProperties.MySQLtablePrefix+"skills WHERE "+lowercase+" > 0 ORDER BY `"+LoadProperties.MySQLtablePrefix+"skills`.`"+lowercase+"` DESC ");    

						for(int i=n;i<=n+10;i++){
							if (i > userslist.size() || mcMMO.database.Read("SELECT user FROM "+LoadProperties.MySQLtablePrefix+"users WHERE id = '" + Integer.valueOf(userslist.get(i).get(1)) + "'") == null)   
								break;
							HashMap<Integer, ArrayList<String>> username =  mcMMO.database.Read("SELECT user FROM "+LoadProperties.MySQLtablePrefix+"users WHERE id = '" + Integer.valueOf(userslist.get(i).get(1)) + "'");   
							player.sendMessage(String.valueOf(i)+". "+ChatColor.GREEN+userslist.get(i).get(0)+" - "+ChatColor.WHITE+username.get(1).get(0));  
						}
						return true;
					}
					//If no page number is specified
					HashMap<Integer, ArrayList<String>> userslist = mcMMO.database.Read("SELECT "+lowercase+", user_id FROM "  
							+LoadProperties.MySQLtablePrefix+"skills WHERE "+lowercase+" > 0 ORDER BY `"+LoadProperties.MySQLtablePrefix+"skills`.`"+lowercase+"` DESC ");    
					for(int i=1;i<=10;i++){ //i<=userslist.size()
						if (i > userslist.size() || mcMMO.database.Read("SELECT user FROM "+LoadProperties.MySQLtablePrefix+"users WHERE id = '" + Integer.valueOf(userslist.get(i).get(1)) + "'") == null)   
							break;
						HashMap<Integer, ArrayList<String>> username =  mcMMO.database.Read("SELECT user FROM "+LoadProperties.MySQLtablePrefix+"users WHERE id = '" + Integer.valueOf(userslist.get(i).get(1)) + "'");   
						player.sendMessage(String.valueOf(i)+". "+ChatColor.GREEN+userslist.get(i).get(0)+" - "+ChatColor.WHITE+username.get(1).get(0));  
					}
					return true;
				}
				if(split.length >= 1){
					player.sendMessage(Messages.getString("mcPlayerListener.PowerLevelLeaderboard")); 
					if(split.length >= 2 && m.isInt(split[1])){
						int n = 1; //For the page number
						int n2 = Integer.valueOf(split[1]);
						if(n2 > 1){
							//Figure out the 'page' here
							n = 10;
							n = n * (n2-1);
						}
						//If a page number is specified
						HashMap<Integer, ArrayList<String>> userslist = mcMMO.database.Read("SELECT "+powerlevel+", user_id FROM "  
								+LoadProperties.MySQLtablePrefix+"skills WHERE "+powerlevel+" > 0 ORDER BY taming+mining+woodcutting+repair+unarmed+herbalism+excavation+archery+swords+axes+acrobatics DESC ");  
						for(int i=n;i<=n+10;i++){
							if (i > userslist.size() || mcMMO.database.Read("SELECT user FROM "+LoadProperties.MySQLtablePrefix+"users WHERE id = '" + Integer.valueOf(userslist.get(i).get(1)) + "'") == null)   
								break;
							HashMap<Integer, ArrayList<String>> username =  mcMMO.database.Read("SELECT user FROM "+LoadProperties.MySQLtablePrefix+"users WHERE id = '" + Integer.valueOf(userslist.get(i).get(1)) + "'");   
							player.sendMessage(String.valueOf(i)+". "+ChatColor.GREEN+userslist.get(i).get(0)+" - "+ChatColor.WHITE+username.get(1).get(0));  
						}
						return true;
					}
					HashMap<Integer, ArrayList<String>> userslist = mcMMO.database.Read("SELECT taming+mining+woodcutting+repair+unarmed+herbalism+excavation+archery+swords+axes+acrobatics, user_id FROM " 
							+LoadProperties.MySQLtablePrefix+"skills WHERE "+powerlevel+" > 0 ORDER BY taming+mining+woodcutting+repair+unarmed+herbalism+excavation+archery+swords+axes+acrobatics DESC ");  
					for(int i=1;i<=10;i++){
						if (i > userslist.size() || mcMMO.database.Read("SELECT user FROM "+LoadProperties.MySQLtablePrefix+"users WHERE id = '" + Integer.valueOf(userslist.get(i).get(1)) + "'") == null)   
							break;
						HashMap<Integer, ArrayList<String>> username =  mcMMO.database.Read("SELECT user FROM "+LoadProperties.MySQLtablePrefix+"users WHERE id = '" + Integer.valueOf(userslist.get(i).get(1)) + "'");   
						player.sendMessage(String.valueOf(i)+". "+ChatColor.GREEN+userslist.get(i).get(0)+" - "+ChatColor.WHITE+username.get(1).get(0));  
						//System.out.println(username.get(1).get(0));
						//System.out.println("Mining : " + userslist.get(i).get(0) + ", User id : " + userslist.get(i).get(1));
					}
				}
			}
		}

		if(LoadProperties.mcrefreshEnable && split[0].equalsIgnoreCase(LoadProperties.mcrefresh)){ 

			if(!mcPermissions.getInstance().mcrefresh(player)){
				player.sendMessage(ChatColor.YELLOW+"[mcMMO]"+ChatColor.DARK_RED +Messages.getString("mcPlayerListener.NoPermission"));  
				return true;
			}
			if(split.length >= 2 && isPlayer(split[1])){
				player.sendMessage("You have refreshed "+split[1]+"'s cooldowns!");  
				player = getPlayer(split[1]);
			}
			/*
			 * PREP MODES
			 */
			PP = Users.getProfile(player);
			PP.setRecentlyHurt((long) 0);
			PP.setHoePreparationMode(false);
			PP.setAxePreparationMode(false);
			PP.setFistsPreparationMode(false);
			PP.setSwordsPreparationMode(false);
			PP.setPickaxePreparationMode(false);
			/*
			 * GREEN TERRA
			 */
			PP.setGreenTerraMode(false);
			PP.setGreenTerraDeactivatedTimeStamp((long) 0);

			/*
			 * GIGA DRILL BREAKER
			 */
			PP.setGigaDrillBreakerMode(false);
			PP.setGigaDrillBreakerDeactivatedTimeStamp((long) 0);
			/*
			 * SERRATED STRIKE
			 */
			PP.setSerratedStrikesMode(false);
			PP.setSerratedStrikesDeactivatedTimeStamp((long) 0);
			/*
			 * SUPER BREAKER
			 */
			PP.setSuperBreakerMode(false);
			PP.setSuperBreakerDeactivatedTimeStamp((long) 0);
			/*
			 * TREE FELLER
			 */
			PP.setTreeFellerMode(false);
			PP.setTreeFellerDeactivatedTimeStamp((long) 0);
			/*
			 * BERSERK
			 */
			PP.setBerserkMode(false);
			PP.setBerserkDeactivatedTimeStamp((long)0);

			player.sendMessage(Messages.getString("mcPlayerListener.AbilitiesRefreshed")); 
		}
		/*
		 * GODMODE COMMAND
		 */
		if(LoadProperties.mcgodEnable && mcPermissions.permissionsEnabled && split[0].equalsIgnoreCase(LoadProperties.mcgod))
		{ 

			if(!mcPermissions.getInstance().mcgod(player))
			{
				player.sendMessage(ChatColor.YELLOW+"[mcMMO]"+ChatColor.DARK_RED +Messages.getString("mcPlayerListener.NoPermission"));  
				return true;
			}
			if(PP.getGodMode())
			{
				player.sendMessage(Messages.getString("mcPlayerListener.GodModeDisabled")); 
				PP.toggleGodMode();
			} else {
				player.sendMessage(Messages.getString("mcPlayerListener.GodModeEnabled")); 
				PP.toggleGodMode();
			}
		}
		if(LoadProperties.clearmyspawnEnable && LoadProperties.enableMySpawn && mcPermissions.getInstance().mySpawn(player) && split[0].equalsIgnoreCase(LoadProperties.clearmyspawn)){ 

			double x = this.getServer().getWorlds().get(0).getSpawnLocation().getX();
			double y = this.getServer().getWorlds().get(0).getSpawnLocation().getY();
			double z = this.getServer().getWorlds().get(0).getSpawnLocation().getZ();
			String worldname = this.getServer().getWorlds().get(0).getName();
			PP.setMySpawn(x, y, z, worldname);
			player.sendMessage(Messages.getString("mcPlayerListener.MyspawnCleared")); 
		}
		if(LoadProperties.mmoeditEnable && mcPermissions.permissionsEnabled && split[0].equalsIgnoreCase(""+LoadProperties.mmoedit)){ 

			if(!mcPermissions.getInstance().mmoedit(player)){
				player.sendMessage(ChatColor.YELLOW+"[mcMMO]"+ChatColor.DARK_RED +Messages.getString("mcPlayerListener.NoPermission"));  
				return true;
			}
			if(split.length < 3){
				player.sendMessage(ChatColor.RED+"Usage is /"+LoadProperties.mmoedit+" playername skillname newvalue");  
				return true;
			}
			if(split.length == 4){
				if(isPlayer(split[1]) && m.isInt(split[3]) && Skills.isSkill(split[2])){
					int newvalue = Integer.valueOf(split[3]);
					Users.getProfile(getPlayer(split[1])).modifyskill(newvalue, split[2]);
					player.sendMessage(ChatColor.RED+split[2]+" has been modified."); 
				}
			}
			else if(split.length == 3){
				if(m.isInt(split[2]) && Skills.isSkill(split[1])){
					int newvalue = Integer.valueOf(split[2]);
					PP.modifyskill(newvalue, split[1]);
					player.sendMessage(ChatColor.RED+split[1]+" has been modified."); 
				}
			} else {
				player.sendMessage(ChatColor.RED+"Usage is /"+LoadProperties.mmoedit+" playername skillname newvalue");  
			}
		}
		/*
		 * ADD EXPERIENCE COMMAND
		 */
		if(LoadProperties.addxpEnable && mcPermissions.permissionsEnabled && split[0].equalsIgnoreCase(LoadProperties.addxp)){ 

			if(!mcPermissions.getInstance().mmoedit(player)){
				player.sendMessage(ChatColor.YELLOW+"[mcMMO]"+ChatColor.DARK_RED +Messages.getString("mcPlayerListener.NoPermission"));  
				return true;
			}
			if(split.length < 3){
				player.sendMessage(ChatColor.RED+"Usage is /"+LoadProperties.addxp+" playername skillname xp");  
				return true;
			}
			if(split.length == 4){
				if(isPlayer(split[1]) && m.isInt(split[3]) && Skills.isSkill(split[2])){
					int newvalue = Integer.valueOf(split[3]);
					Users.getProfile(getPlayer(split[1])).addXP(split[2], newvalue);
					getPlayer(split[1]).sendMessage(ChatColor.GREEN+"Experience granted!"); 
					player.sendMessage(ChatColor.RED+split[2]+" has been modified."); 
					Skills.XpCheck(getPlayer(split[1]));
				}
			}
			else if(split.length == 3 && m.isInt(split[2]) && Skills.isSkill(split[1])){
				int newvalue = Integer.valueOf(split[2]);
				Users.getProfile(player).addXP(split[1], newvalue);
				player.sendMessage(ChatColor.RED+split[1]+" has been modified."); 
			} else {
				player.sendMessage(ChatColor.RED+"Usage is /"+LoadProperties.addxp+" playername skillname xp");  
			}
		}
		if(LoadProperties.ptpEnable && PP != null && PP.inParty() && split[0].equalsIgnoreCase(LoadProperties.ptp)){ 

			if(!mcPermissions.getInstance().partyTeleport(player)){
				player.sendMessage(ChatColor.YELLOW+"[mcMMO]"+ChatColor.DARK_RED +Messages.getString("mcPlayerListener.NoPermission"));  
				return true;
			}
			if(split.length < 2){
				player.sendMessage(ChatColor.RED+"Usage is /"+LoadProperties.ptp+" <playername>");  
				return true;
			}
			if(!isPlayer(split[1])){
				player.sendMessage("That is not a valid player"); 
			}
			if(isPlayer(split[1])){
				Player target = getPlayer(split[1]);
				PlayerProfile PPt = Users.getProfile(target);
				if(PP.getParty().equals(PPt.getParty())){
					player.teleport(target);
					player.sendMessage(ChatColor.GREEN+"You have teleported to "+target.getName()); 
					target.sendMessage(ChatColor.GREEN+player.getName() + " has teleported to you."); 
				}
			}
		}
		/*
		 * WHOIS COMMAND
		 */
		if(LoadProperties.whoisEnable && (player.isOp() || mcPermissions.getInstance().whois(player)) && split[0].equalsIgnoreCase(LoadProperties.whois)){ 
			if(split.length < 2){
				player.sendMessage(ChatColor.RED + "Proper usage is /"+LoadProperties.whois+" <playername>");  
				return true;
			}
			//if split[1] is a player
			if(isPlayer(split[1]))
			{
				Player target = getPlayer(split[1]);
				PlayerProfile PPt = Users.getProfile(target);
				double x,y,z;
				x = target.getLocation().getX();
				y = target.getLocation().getY();
				z = target.getLocation().getZ();
				player.sendMessage(ChatColor.GREEN + "~~WHOIS RESULTS~~"); 
				player.sendMessage(target.getName());
				if(PPt.inParty())
					player.sendMessage("Party: "+PPt.getParty()); 
				player.sendMessage("Health: "+target.getHealth()+ChatColor.GRAY+" (20 is full health)");  
				player.sendMessage("OP: " + target.isOp()); 
				player.sendMessage(ChatColor.GREEN+"mcMMO Stats for "+ChatColor.YELLOW+target.getName()); 

				player.sendMessage(ChatColor.GOLD+"-=GATHERING SKILLS=-");
				if(mcPermissions.getInstance().excavation(target))
					player.sendMessage(Skills.getSkillStats(Messages.getString("mcPlayerListener.ExcavationSkill"), PPt.getSkillToString("excavation"), PPt.getSkillToString("excavationXP"), PPt.getXpToLevel("excavation")));
				if(mcPermissions.getInstance().herbalism(target))
					player.sendMessage(Skills.getSkillStats(Messages.getString("mcPlayerListener.HerbalismSkill"), PPt.getSkillToString("herbalism"), PPt.getSkillToString("herbalismXP"), PPt.getXpToLevel("herbalism")));
				if(mcPermissions.getInstance().mining(target))
					player.sendMessage(Skills.getSkillStats(Messages.getString("mcPlayerListener.MiningSkill"), PPt.getSkillToString("mining"), PPt.getSkillToString("miningXP"), PPt.getXpToLevel("mining")));
				if(mcPermissions.getInstance().woodCuttingAbility(target))
					player.sendMessage(Skills.getSkillStats(Messages.getString("mcPlayerListener.WoodcuttingSkill"), PPt.getSkillToString("woodcutting"), PPt.getSkillToString("woodcuttingXP"), PPt.getXpToLevel("woodcutting")));

				player.sendMessage(ChatColor.GOLD+"-=COMBAT SKILLS=-");
				if(mcPermissions.getInstance().axes(target))
					player.sendMessage(Skills.getSkillStats(Messages.getString("mcPlayerListener.AxesSkill"), PPt.getSkillToString("axes"), PPt.getSkillToString("axesXP"), PPt.getXpToLevel("axes")));
				if(mcPermissions.getInstance().archery(player))
					player.sendMessage(Skills.getSkillStats(Messages.getString("mcPlayerListener.ArcherySkill"), PPt.getSkillToString("archery"), PPt.getSkillToString("archeryXP"), PPt.getXpToLevel("archery")));
				//if(mcPermissions.getInstance().sorcery(target))
				//player.sendMessage(Skills.getSkillStats(Messages.getString("mcPlayerListener.SorcerySkill"), PPt.getSkillToString("sorcery"), PPt.getSkillToString("sorceryXP"), PPt.getXpToLevel("excavation")));
				if(mcPermissions.getInstance().swords(target))
					player.sendMessage(Skills.getSkillStats(Messages.getString("mcPlayerListener.SwordsSkill"), PPt.getSkillToString("swords"), PPt.getSkillToString("swordsXP"), PPt.getXpToLevel("swords")));
				if(mcPermissions.getInstance().taming(target))
					player.sendMessage(Skills.getSkillStats(Messages.getString("mcPlayerListener.TamingSkill"), PPt.getSkillToString("taming"), PPt.getSkillToString("tamingXP"), PPt.getXpToLevel("taming")));
				if(mcPermissions.getInstance().unarmed(target))
					player.sendMessage(Skills.getSkillStats(Messages.getString("mcPlayerListener.UnarmedSkill"), PPt.getSkillToString("unarmed"), PPt.getSkillToString("unarmedXP"), PPt.getXpToLevel("unarmed")));

				player.sendMessage(ChatColor.GOLD+"-=MISC SKILLS=-");
				if(mcPermissions.getInstance().acrobatics(target))
					player.sendMessage(Skills.getSkillStats(Messages.getString("mcPlayerListener.AcrobaticsSkill"), PPt.getSkillToString("acrobatics"), PPt.getSkillToString("acrobaticsXP"), PPt.getXpToLevel("acrobatics")));
				if(mcPermissions.getInstance().repair(target))
					player.sendMessage(Skills.getSkillStats(Messages.getString("mcPlayerListener.RepairSkill"), PPt.getSkillToString("repair"), PPt.getSkillToString("repairXP"), PPt.getXpToLevel("repair")));	

				player.sendMessage(Messages.getString("mcPlayerListener.PowerLevel") +ChatColor.GREEN+(m.getPowerLevel(target))); 

				player.sendMessage(ChatColor.GREEN+"~~COORDINATES~~"); 
				player.sendMessage("X: "+x); 
				player.sendMessage("Y: "+y); 
				player.sendMessage("Z: "+z); 
			}
		}
		/*
		 * STATS COMMAND
		 */
		if(LoadProperties.statsEnable && split[0].equalsIgnoreCase(LoadProperties.stats)){ 

			player.sendMessage(Messages.getString("mcPlayerListener.YourStats")); 
			if(mcPermissions.getInstance().permissionsEnabled)
				player.sendMessage(Messages.getString("mcPlayerListener.NoSkillNote")); 

			ChatColor header = ChatColor.GOLD;

			if(Skills.hasGatheringSkills(player)){
				player.sendMessage(header+"-=GATHERING SKILLS=-");
				if(mcPermissions.getInstance().excavation(player))
					player.sendMessage(Skills.getSkillStats(Messages.getString("mcPlayerListener.ExcavationSkill"), PP.getSkillToString("excavation"), PP.getSkillToString("excavationXP"), PP.getXpToLevel("excavation")));
				if(mcPermissions.getInstance().herbalism(player))
					player.sendMessage(Skills.getSkillStats(Messages.getString("mcPlayerListener.HerbalismSkill"), PP.getSkillToString("herbalism"), PP.getSkillToString("herbalismXP"), PP.getXpToLevel("herbalism")));
				if(mcPermissions.getInstance().mining(player))
					player.sendMessage(Skills.getSkillStats(Messages.getString("mcPlayerListener.MiningSkill"), PP.getSkillToString("mining"), PP.getSkillToString("miningXP"), PP.getXpToLevel("mining")));
				if(mcPermissions.getInstance().woodCuttingAbility(player))
					player.sendMessage(Skills.getSkillStats(Messages.getString("mcPlayerListener.WoodcuttingSkill"), PP.getSkillToString("woodcutting"), PP.getSkillToString("woodcuttingXP"), PP.getXpToLevel("woodcutting")));
			}
			if(Skills.hasCombatSkills(player)){
				player.sendMessage(header+"-=COMBAT SKILLS=-");
				if(mcPermissions.getInstance().axes(player))
					player.sendMessage(Skills.getSkillStats(Messages.getString("mcPlayerListener.AxesSkill"), PP.getSkillToString("axes"), PP.getSkillToString("axesXP"), PP.getXpToLevel("axes")));
				if(mcPermissions.getInstance().archery(player))
					player.sendMessage(Skills.getSkillStats(Messages.getString("mcPlayerListener.ArcherySkill"), PP.getSkillToString("archery"), PP.getSkillToString("archeryXP"), PP.getXpToLevel("archery")));
				//if(mcPermissions.getInstance().sorcery(player))
				//player.sendMessage(Skills.getSkillStats(Messages.getString("mcPlayerListener.SorcerySkill"), PP.getSkillToString("sorcery"), PP.getSkillToString("sorceryXP"), PP.getXpToLevel("excavation")));
				if(mcPermissions.getInstance().swords(player))
					player.sendMessage(Skills.getSkillStats(Messages.getString("mcPlayerListener.SwordsSkill"), PP.getSkillToString("swords"), PP.getSkillToString("swordsXP"), PP.getXpToLevel("swords")));
				if(mcPermissions.getInstance().taming(player))
					player.sendMessage(Skills.getSkillStats(Messages.getString("mcPlayerListener.TamingSkill"), PP.getSkillToString("taming"), PP.getSkillToString("tamingXP"), PP.getXpToLevel("taming")));
				if(mcPermissions.getInstance().unarmed(player))
					player.sendMessage(Skills.getSkillStats(Messages.getString("mcPlayerListener.UnarmedSkill"), PP.getSkillToString("unarmed"), PP.getSkillToString("unarmedXP"), PP.getXpToLevel("unarmed")));
			}

			if(Skills.hasMiscSkills(player)){
				player.sendMessage(header+"-=MISC SKILLS=-");
				if(mcPermissions.getInstance().acrobatics(player))
					player.sendMessage(Skills.getSkillStats(Messages.getString("mcPlayerListener.AcrobaticsSkill"), PP.getSkillToString("acrobatics"), PP.getSkillToString("acrobaticsXP"), PP.getXpToLevel("acrobatics")));
				if(mcPermissions.getInstance().repair(player))
					player.sendMessage(Skills.getSkillStats(Messages.getString("mcPlayerListener.RepairSkill"), PP.getSkillToString("repair"), PP.getSkillToString("repairXP"), PP.getXpToLevel("repair")));	
			}
			player.sendMessage(Messages.getString("mcPlayerListener.PowerLevel")+ChatColor.GREEN+(m.getPowerLevel(player))); 
		}
		//Invite Command
		if(LoadProperties.inviteEnable && mcPermissions.getInstance().party(player) && split[0].equalsIgnoreCase(LoadProperties.invite)){ 

			if(!PP.inParty()){
				player.sendMessage(Messages.getString("mcPlayerListener.NotInParty")); 
				return true;
			}
			if(split.length < 2){
				player.sendMessage(ChatColor.RED+"Usage is /"+LoadProperties.invite+" <playername>");  
				return true;
			}
			if(PP.inParty() && split.length >= 2 && isPlayer(split[1])){
				Player target = getPlayer(split[1]);
				PlayerProfile PPt = Users.getProfile(target);
				PPt.modifyInvite(PP.getParty());
				player.sendMessage(Messages.getString("mcPlayerListener.InviteSuccess")); 
				//target.sendMessage(ChatColor.RED+"ALERT: "+ChatColor.GREEN+"You have received a party invite for "+PPt.getInvite()+" from "+player.getName());   
				target.sendMessage(Messages.getString("mcPlayerListener.ReceivedInvite1", new Object[] {PPt.getInvite(), player.getName()}));
				//target.sendMessage(ChatColor.YELLOW+"Type "+ChatColor.GREEN+LoadProperties.accept+ChatColor.YELLOW+" to accept the invite");   
				target.sendMessage(Messages.getString("mcPlayerListener.ReceivedInvite2", new Object[] {LoadProperties.accept}));
			}
		}
		//Accept invite
		if(LoadProperties.acceptEnable && mcPermissions.getInstance().party(player) && split[0].equalsIgnoreCase(LoadProperties.accept)){ 
			if(PP.hasPartyInvite()){
				if(PP.inParty()){
					Party.getInstance().informPartyMembersQuit(player, getPlayersOnline());
				}
				PP.acceptInvite();
				Party.getInstance().informPartyMembers(player, getPlayersOnline());
				player.sendMessage(Messages.getString("mcPlayerListener.InviteAccepted", new Object[]{PP.getParty()}));  
			} else {
				player.sendMessage(Messages.getString("mcPlayerListener.NoInvites")); 
			}
		}
		//Party command
		if(LoadProperties.partyEnable && split[0].equalsIgnoreCase(LoadProperties.party)){ 
			if(!mcPermissions.getInstance().party(player)){
				player.sendMessage(ChatColor.YELLOW+"[mcMMO]"+ChatColor.DARK_RED +Messages.getString("mcPlayerListener.NoPermission"));  
				return true;
			}
			if(split.length == 1 && !PP.inParty()){
				player.sendMessage("Proper usage is "+LoadProperties.party+" <name> or 'q' to quit");   
				return true;
			}
			if(split.length == 1 && PP.inParty()){
				String tempList = ""; 
				int x = 0;
				for(Player p : this.getServer().getOnlinePlayers()){
					if(PP.getParty().equals(Users.getProfile(p).getParty())){
						if(p != null && x+1 >= Party.getInstance().partyCount(player, getPlayersOnline())){
							tempList+= p.getName();
							x++;
						}
						if(p != null && x < Party.getInstance().partyCount(player, getPlayersOnline())){
							tempList+= p.getName() +", "; 
							x++;
						}
					}
				}
				player.sendMessage(Messages.getString("mcPlayerListener.YouAreInParty", new Object[] {PP.getParty()}));
				player.sendMessage(Messages.getString("mcPlayerListener.PartyMembers")+" ("+ChatColor.WHITE+tempList+ChatColor.GREEN+")");  
			}
			if(split.length > 1 && split[1].equals("q") && PP.inParty()){ 
				Party.getInstance().informPartyMembersQuit(player, getPlayersOnline());
				PP.removeParty();
				player.sendMessage(Messages.getString("mcPlayerListener.LeftParty")); 
				return true;
			}
			if(split.length >= 2){
				if(PP.inParty())
					Party.getInstance().informPartyMembersQuit(player, getPlayersOnline());
				PP.setParty(split[1]);
				player.sendMessage(Messages.getString("mcPlayerListener.JoinedParty", new Object[] {split[1]}));
				Party.getInstance().informPartyMembers(player, getPlayersOnline());
			}
		}
		if(LoadProperties.partyEnable && split[0].equalsIgnoreCase("p")){

			if(!mcPermissions.getInstance().party(player)){
				player.sendMessage(ChatColor.YELLOW+"[mcMMO]"+ChatColor.DARK_RED +Messages.getString("mcPlayerListener.NoPermission"));  
				return true;
			}
			if(PP.getAdminChatMode())
				PP.toggleAdminChat();

			PP.togglePartyChat();

			if(PP.getPartyChatMode()){
				//player.sendMessage(ChatColor.GREEN + "Party Chat Toggled On"); 
				player.sendMessage(Messages.getString("mcPlayerListener.PartyChatOn"));
			} else {
				//player.sendMessage(ChatColor.GREEN + "Party Chat Toggled " + ChatColor.RED + "Off");  
				player.sendMessage(Messages.getString("mcPlayerListener.PartyChatOff"));
			}
		}

		if(split[0].equalsIgnoreCase("a") && (player.isOp() || mcPermissions.getInstance().adminChat(player))){
			if(!mcPermissions.getInstance().adminChat(player) && !player.isOp()){
				player.sendMessage(ChatColor.YELLOW+"[mcMMO]"+ChatColor.DARK_RED +Messages.getString("mcPlayerListener.NoPermission"));  
				return true;
			}

			if(PP.getPartyChatMode())
				PP.togglePartyChat();

			PP.toggleAdminChat();

			if(PP.getAdminChatMode())
			{
				player.sendMessage(Messages.getString("mcPlayerListener.AdminChatOn"));
				//player.sendMessage(ChatColor.AQUA + "Admin chat toggled " + ChatColor.GREEN + "On");  
			} else {
				player.sendMessage(Messages.getString("mcPlayerListener.AdminChatOff"));
				//player.sendMessage(ChatColor.AQUA + "Admin chat toggled " + ChatColor.RED + "Off");  
			}
		}

		/*
		 * MYSPAWN
		 */
		if(LoadProperties.myspawnEnable && LoadProperties.enableMySpawn && split[0].equalsIgnoreCase(LoadProperties.myspawn)){ 
			if(!mcPermissions.getInstance().mySpawn(player)){
				player.sendMessage(ChatColor.YELLOW+"[mcMMO]"+ChatColor.DARK_RED +Messages.getString("mcPlayerListener.NoPermission"));  
				return true;
			}
			if(System.currentTimeMillis() < PP.getMySpawnATS() + 3600000){
				long x = ((PP.getMySpawnATS() + 3600000) - System.currentTimeMillis());
				int y = (int) (x/60000);
				int z = (int) ((x/1000) - (y*60));
				player.sendMessage(Messages.getString("mcPlayerListener.MyspawnTimeNotice", new Object[] {y, z}));    
				return true;
			}
			PP.setMySpawnATS(System.currentTimeMillis());
			if(PP.getMySpawn(player) != null){
				Location mySpawn = PP.getMySpawn(player);
				if(mySpawn != null && this.getServer().getWorld(PP.getMySpawnWorld(this)) != null)
					mySpawn.setWorld(this.getServer().getWorld(PP.getMySpawnWorld(this)));
				if(mySpawn != null){
					//It's done twice because it acts oddly when you are in another world
					player.teleport(mySpawn);
					player.teleport(mySpawn);
				}
			} else {
				player.sendMessage(Messages.getString("mcPlayerListener.MyspawnNotExist")); 
			}
		}
		long after = System.currentTimeMillis();
		if(LoadProperties.print_reports){
			this.onPlayerCommandPreprocess+=(after-before);
		}
		return true;
	}

	public Player[] getPlayersOnline() {
		return this.getServer().getOnlinePlayers();
	}

	public boolean isPlayer(String playerName){
		for(Player herp :  getPlayersOnline()){
			if(herp.getName().toLowerCase().equals(playerName.toLowerCase())){
				return true;
			}
		}
		return false;
	}

	public Player getPlayer(String playerName){
		for(Player herp : getPlayersOnline()){
			if(herp.getName().toLowerCase().equals(playerName.toLowerCase())){
				return herp;
			}
		}
		return null;
	}
}