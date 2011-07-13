package com.gmail.nossr50;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import com.gmail.nossr50.config.LoadProperties;
import com.gmail.nossr50.datatypes.PlayerProfile;
import com.gmail.nossr50.party.Party;
import com.gmail.nossr50.skills.Herbalism;
import com.gmail.nossr50.skills.Repair;
import com.gmail.nossr50.skills.Skills;


public class mcPlayerListener extends PlayerListener {
	protected static final Logger log = Logger.getLogger("Minecraft"); //$NON-NLS-1$
	public Location spawn = null;
	private mcMMO plugin;

	public mcPlayerListener(mcMMO instance) {
		plugin = instance;
	}


	public void onPlayerRespawn(PlayerRespawnEvent event) 
	{
		long before = System.currentTimeMillis();
		Player player = event.getPlayer();
		if(LoadProperties.enableMySpawn && mcPermissions.getInstance().mySpawn(player))
		{
			PlayerProfile PP = Users.getProfile(player);

			if(player != null && PP != null)
			{
				PP.setRespawnATS(System.currentTimeMillis());
				Location mySpawn = PP.getMySpawn(player);
				if(mySpawn != null && plugin.getServer().getWorld(PP.getMySpawnWorld(plugin)) != null)
					mySpawn.setWorld(plugin.getServer().getWorld(PP.getMySpawnWorld(plugin)));
				if(mySpawn != null)
				{
					event.setRespawnLocation(mySpawn);
				}
			}
		}
		long after = System.currentTimeMillis();
		if(LoadProperties.print_reports)
		{
			plugin.onPlayerRespawn+=(after-before);
		}
	}
	public void onPlayerLogin(PlayerLoginEvent event) 
	{
		long before = System.currentTimeMillis();
		Users.addUser(event.getPlayer());
		long after = System.currentTimeMillis();

		if(LoadProperties.print_reports)
		{
			plugin.onPlayerLogin+=(after-before);
		}
	}

	public void onPlayerQuit(PlayerQuitEvent event) 
	{
		long before = System.currentTimeMillis();
		/*
		 * GARBAGE COLLECTION
		 */

		 //Discard the PlayerProfile object from players array in the Users object stored for this player as it is no longer needed
		Users.removeUser(event.getPlayer());
		long after = System.currentTimeMillis();

		if(LoadProperties.print_reports)
		{
			plugin.onPlayerQuit+=(after-before);
		}
	}

	public void onPlayerJoin(PlayerJoinEvent event) 
	{
		long before = System.currentTimeMillis();
		Player player = event.getPlayer();

		if(mcPermissions.getInstance().motd(player) && LoadProperties.enableMotd)
		{
			//player.sendMessage(ChatColor.BLUE +"This server is running mcMMO "+plugin.getDescription().getVersion()+" type /"+ChatColor.YELLOW+LoadProperties.mcmmo+ChatColor.BLUE+ " for help.");
			player.sendMessage(Messages.getString("mcPlayerListener.MOTD", new Object[] {plugin.getDescription().getVersion(), LoadProperties.mcmmo}));
			//player.sendMessage(ChatColor.GREEN+"http://mcmmo.wikia.com"+ChatColor.BLUE+" - mcMMO Wiki");
			player.sendMessage(Messages.getString("mcPlayerListener.WIKI"));
		}
		long after = System.currentTimeMillis();
		if(LoadProperties.print_reports)
		{
			plugin.onPlayerJoin+=(after-before);
		}
	}

	@SuppressWarnings("deprecation")
	public void onPlayerInteract(PlayerInteractEvent event) {
		long before = System.currentTimeMillis();

		Player player = event.getPlayer();
		PlayerProfile PP = Users.getProfile(player);
		Action action = event.getAction();
		Block block = event.getClickedBlock();



		//Archery Nerf
		if(player.getItemInHand().getTypeId() == 261 && LoadProperties.archeryFireRateLimit){
			if(System.currentTimeMillis() < PP.getArcheryShotATS() + 1000){
				/*
    			if(m.hasArrows(player))
    				m.addArrows(player);
				 */
				player.updateInventory();
				event.setCancelled(true);
			} else {
				PP.setArcheryShotATS(System.currentTimeMillis());
			}
		}

		/*
		 * Ability checks
		 */
		if(action == Action.RIGHT_CLICK_BLOCK)
		{
			ItemStack is = player.getItemInHand();
			if(LoadProperties.enableMySpawn && block != null && player != null)
			{
				if(block.getTypeId() == 26 && mcPermissions.getInstance().setMySpawn(player)){
					Location loc = player.getLocation();
					if(mcPermissions.getInstance().setMySpawn(player)){
						PP.setMySpawn(loc.getX(), loc.getY(), loc.getZ(), loc.getWorld().getName());
					}
					player.sendMessage(Messages.getString("mcPlayerListener.MyspawnSet"));
				}
			}

			if(block != null && player != null && mcPermissions.getInstance().repair(player) && event.getClickedBlock().getTypeId() == 42)
			{
				Repair.repairCheck(player, is, event.getClickedBlock());
			}

			if(m.abilityBlockCheck(block))
			{
				if(block != null && m.isHoe(player.getItemInHand()) && block.getTypeId() != 3 && block.getTypeId() != 2 && block.getTypeId() != 60){
					Skills.hoeReadinessCheck(player);
				}
				Skills.abilityActivationCheck(player);
			}

			//GREEN THUMB
			if(block != null && (block.getType() == Material.COBBLESTONE || block.getType() == Material.DIRT) && player.getItemInHand().getType() == Material.SEEDS)
			{
				boolean pass = false;
				if(Herbalism.hasSeeds(player) && mcPermissions.getInstance().herbalism(player)){
					Herbalism.removeSeeds(player);
					if(LoadProperties.enableCobbleToMossy && m.blockBreakSimulate(block, player, plugin) && block.getType() == Material.COBBLESTONE && Math.random() * 1500 <= PP.getSkill("herbalism")){
						player.sendMessage(Messages.getString("mcPlayerListener.GreenThumb"));
						block.setType(Material.MOSSY_COBBLESTONE);
						pass = true;
					}
					if(block.getType() == Material.DIRT && m.blockBreakSimulate(block, player, plugin) && Math.random() * 1500 <= PP.getSkill("herbalism")){
						player.sendMessage(Messages.getString("mcPlayerListener.GreenThumb"));
						block.setType(Material.GRASS);
						pass = true;
					}
					if(pass == false)
						player.sendMessage(Messages.getString("mcPlayerListener.GreenThumbFail"));
				}
				return;
			}
		}
		if(action == Action.RIGHT_CLICK_AIR)
		{
			Skills.hoeReadinessCheck(player);
			Skills.abilityActivationCheck(player);

			/*
			 * HERBALISM MODIFIERS
			 */
			if(mcPermissions.getInstance().herbalism(player))
			{
				Herbalism.breadCheck(player, player.getItemInHand());
				Herbalism.stewCheck(player, player.getItemInHand());
			}
		}
		/*
    	if(action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK)
    	{
    		if(mcPermissions.getInstance().sorcery(player))
    		{
    			//FIND OUT WHAT SPELL THEY ARE USING
    			if(player.getItemInHand().getTypeId() == 351)
    			{
    				//IF USING LIGHT BLUE DYE (WATER MAGIC)
    				if(player.getItemInHand().getDurability() == (short) 12)
    				{
    					if(PP.getBlueDyeCycle() == 0 && PP.getCurrentMana() >= LoadProperties.water_thunder)
    					{
    						if(plugin.sorcery.getSpellTargetBlock(player) != null)
    						{
    							PP.setMana(PP.getCurrentMana() - LoadProperties.water_thunder);

    							Block targetBlock = plugin.sorcery.getSpellTargetBlock(player);
    							World world = targetBlock.getLocation().getWorld();

    							world.strikeLightning(targetBlock.getLocation()); 							
    							world.strikeLightning(targetBlock.getLocation());
    							world.strikeLightning(targetBlock.getLocation());

    							world.createExplosion(targetBlock.getLocation(), (float) 4, true);

    							plugin.sorcery.informSpell(Messages.getString("Sorcery.Water.Thunder"), player);
    							plugin.sorcery.shoutSpell(Messages.getString("Sorcery.Water.Thunder"), player);
    						}
    					} 
    					else if (PP.getBlueDyeCycle() == 0 && PP.getCurrentMana() < LoadProperties.water_thunder )
    					{
    						player.sendMessage(Messages.getString("Sorcery.OOM", new Object[] {PP.getCurrentMana(), LoadProperties.water_thunder, Messages.getString("Sorcery.Water.Thunder")}));
    					}
    				}
    				//IF USING LIGHT GREEN DYE (RESTORATIVE MAGIC)
    				if(player.getItemInHand().getDurability() == (short) 10)
    				{
    					//CHECK WHICH SPELL

    					//HEAL SELF SPELL
    					if(PP.getGreenDyeCycle() == 0 && mcPermissions.getInstance().sorceryCurativeHealSelf(player))
    					{
    						//SPELL COST
    						if(PP.getCurrentMana() >= LoadProperties.cure_self)
    						{
    							PP.setMana(PP.getCurrentMana() - LoadProperties.cure_self);
    							player.setHealth(player.getHealth()+2);
    							if(player.getHealth() > 20)
    								player.setHealth(20);

    							//XP
    							PP.addSorceryXP((LoadProperties.cure_self/PP.getMaxMana()) * 100);
    							Skills.XpCheck(player);

    							plugin.sorcery.informSpell(Messages.getString("Sorcery.Curative.Self"), player);
    							plugin.sorcery.shoutSpell(Messages.getString("Sorcery.Curative.Self"), player);
    						} 
    						else
    						{
    							player.sendMessage(Messages.getString("Sorcery.OOM", new Object[] {PP.getCurrentMana(), LoadProperties.cure_self, Messages.getString("Sorcery.Curative.Self")}));
    						}
    					}
    					//HEAL OTHER SPELL
    					if(PP.getGreenDyeCycle() == 1)
    					{
    						//SPELL COST
    						if(PP.getCurrentMana() >= LoadProperties.cure_other)
    						{
    							PP.setMana(PP.getCurrentMana() - LoadProperties.cure_other);

    							//XP
    							PP.addSorceryXP((LoadProperties.cure_other/PP.getMaxMana()) * 100);
    							Skills.XpCheck(player);

    							plugin.sorcery.informSpell(Messages.getString("Sorcery.Curative.Other"), player);
    							plugin.sorcery.shoutSpell(Messages.getString("Sorcery.Curative.Other"), player);
    						}
    						else
    						{
    							player.sendMessage(Messages.getString("Sorcery.OOM", new Object[] {PP.getCurrentMana(), LoadProperties.cure_other, Messages.getString("Sorcery.Curative.Other")}));
    						}
    					}
    				}
    			}
    		}
    	}
		 */

		/*
    	if(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)
    	{
    		if(mcPermissions.getInstance().sorcery(player))
    		{
    			//FIND OUT WHAT SPELL THEY ARE USING
    			if(player.getItemInHand().getTypeId() == 351)
    			{
    				//IF USING LIGHT GREEN DYE (RESTORATIVE MAGIC)
    				if(mcPermissions.getInstance().sorceryCurative(player) && player.getItemInHand().getDurability() == (short) 10)
    				{

    					while(PP.getDyeChanged() == false)
    					{
    						plugin.sorcery.handleGreenDyeCycle(player);
    					}

    					//CHANGE BACK
    					PP.setDyeChanged(false);
    				}
    			}
    		}
    	}
		 */

		/*
		 * ITEM CHECKS
		 */
		if(action == Action.RIGHT_CLICK_AIR)
			Item.itehecks(player, plugin);
		if(action == Action.RIGHT_CLICK_BLOCK)
		{
			if(m.abilityBlockCheck(event.getClickedBlock()))
				Item.itehecks(player, plugin);
		}
		long after = System.currentTimeMillis();
		if(LoadProperties.print_reports)
		{
			plugin.onPlayerInteract+=(after-before);
		}
	}

	public void onPlayerChat(PlayerChatEvent event) 
	{
		long before = System.currentTimeMillis();
		Player player = event.getPlayer();
		PlayerProfile PP = Users.getProfile(player);

		String x = ChatColor.GREEN + "(" + ChatColor.WHITE + player.getName() + ChatColor.GREEN + ") "; //$NON-NLS-1$ //$NON-NLS-2$
		String y = ChatColor.AQUA + "{" + ChatColor.WHITE + player.getName() + ChatColor.AQUA + "} "; //$NON-NLS-1$ //$NON-NLS-2$

		if(PP.getPartyChatMode())
		{
			event.setCancelled(true);
			log.log(Level.INFO, "[P]("+PP.getParty()+")"+"<"+player.getName()+"> "+event.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			for(Player herp : plugin.getServer().getOnlinePlayers())
			{
				if(Users.getProfile(herp).inParty())
				{
					if(Party.getInstance().inSameParty(herp, player))
					{
						herp.sendMessage(x+event.getMessage());
					}
				}
			}
			return;
		}

		if((player.isOp() || mcPermissions.getInstance().adminChat(player)) && PP.getAdminChatMode())
		{
			log.log(Level.INFO, "[A]"+"<"+player.getName()+"> "+event.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			event.setCancelled(true);
			for(Player herp : plugin.getServer().getOnlinePlayers()){
				if((herp.isOp() || mcPermissions.getInstance().adminChat(herp))){
					herp.sendMessage(y+event.getMessage());
				}
			}
			return;
		}
		long after = System.currentTimeMillis();
		if(LoadProperties.print_reports)
		{
			plugin.onPlayerChat+=(after-before);
		}
	}
}