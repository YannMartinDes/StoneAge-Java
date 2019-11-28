package game;

import game.zone.Zone;
import game.zone.ZoneBuilding;
import game.zone.ZoneCarteCivilisation;
import game.zone.ZoneField;
import game.zone.ZoneHunt;
import game.zone.ZoneHut;
import game.zone.ZoneRessource;
import game.zone.ZoneTool;
import inventory.Inventory;
import player.Player;
import printer.Printer;

/**
 * GameZones represente le plateau cote zone du jeu.
 * @author Mentra20
 *
 */
public class GameZones {

	/* FIELD */
	private Zone[] zones;//Tableau des zones.
	private CarteCivilisationManager cardManager; //gestion carte civilisation
	int numberPlayer;
	private final Dice dice;

	/* CONSTRUCTOR */
	public GameZones(int numberPlayer, Dice dice) {
		this.numberPlayer=numberPlayer;
		this.dice = dice;
		initZone();
	}

	/* GETTERS */
	public CarteCivilisationManager getCardManager() { return cardManager; }
	public Zone[] getZones() {return zones;}


	/**
	 * gestion du placement du joueur dans une zone (selection du joueur + placement)
	 * @param player le joueur qui doit placer les figurine
	 * @return true : n'a plus de figurine a placer ; ; 
	 * false : a encore des figurine a placer
	 */
	public boolean playerPlaceFigurine(Player player) {
		Printer.getPrinter().println("\nC'est le tour de " + player.getName() + ".");
		int zoneIndex = zoneChoose(player);//On recupere l'indice de la zone
		int number = numberChoose(player,zones[zoneIndex]);//On recupere le nombre de figurines
		zones[zoneIndex].placeFigurine(number,player);//On place les figurines dans la zone. 

		if(player.getCurrentFigurine() == 0) {//Si le joueur n'a plus de figurine
			return true;
		}
		return false;
	}


	/**
	 * Phase de recolte, le joueur selectionner recupere toutes ces figurine et recois les effet de la zone en question
	 * @param player le joueur qui recupere
	 * @param inventory l'inventaire du joueur en question
	 */
	public void playerHarvest(Player player, Inventory inventory){
		Printer.getPrinter().println("\nC'est au tour de "+player.getName()+" :");

		for(int i = 0; i < zones.length; i++) 
		{
			if(zones[i].howManyPlayerFigurine(player) != 0) //Si le joueur avait des figurines dans la zone. 
			{
				zones[i].playerRecoveryFigurine(player,inventory);//Il recupere ses figurines et les ressources.
			}
		}
	}


	/**
	 * Initie le tableau zones. 
	 */
	public void initZone(){
		zones = new Zone[Settings.NB_ZONES];
		ZoneCarteCivilisation[] zoneCarteCivilisation = new ZoneCarteCivilisation[4];

		//Les zones du jeu. 
		zones[0] = new ZoneRessource("Foret",Ressource.WOOD,Settings.MAX_ZONERESSOURCE_SPACE, dice);
		zones[1] = new ZoneRessource("Glaisiere",Ressource.CLAY,Settings.MAX_ZONERESSOURCE_SPACE, dice);
		zones[2] = new ZoneRessource("Carriere",Ressource.STONE,Settings.MAX_ZONERESSOURCE_SPACE, dice);
		zones[3] = new ZoneRessource("Riviere",Ressource.GOLD,Settings.MAX_ZONERESSOURCE_SPACE, dice);
		//La zone de chasse a : nombre de joueur x le nombre de figurines maximum d'espace. 
		zones[4] = new ZoneHunt("Chasse", Ressource.FOOD,numberPlayer * Settings.MAX_FIGURINE, dice);
		zones[5] = new ZoneField("Champs", Ressource.FIELD);
		zones[6] = new ZoneHut("Cabane de reproduction");
		zones[7] = new ZoneTool("Le Fabricant D'outils");

		//gestion des carte civilisation
		ZoneCarteCivilisation zcc;
		for(int i=0;i<4;i++) {
			zcc=new ZoneCarteCivilisation("Carte Civilisation "+ (i+1), i+1, dice);
			zoneCarteCivilisation[i]=zcc;
			zones[8+i]=zcc;
		}
		cardManager = new CarteCivilisationManager(zoneCarteCivilisation);

		zones[12] = new ZoneBuilding("Tuile Batiment 1");
		zones[13] = new ZoneBuilding("Tuile Batiment 2");
		zones[14] = new ZoneBuilding("Tuile Batiment 3");
		zones[15] = new ZoneBuilding("Tuile Batiment 4");

	}


	/**
	 * mise a jour des carte dans les zone carteCivilisation
	 */
	public void organizeCard() {
		cardManager.organizeCard();
	}


	/* END? */
	/**
	 * Renvoie true ou false, si la condition de victoire a ete effectue. 
	 * @return true si le jeu doit se terminer, false sinon
	 */
	public boolean isEnd(){
		// DANS LES CARTE CIVILISATION
		if(cardManager.isEmpty()) {
			Printer.getPrinter().println("\n--- PARTIE TERMINEE : Il n'y a plus de carte civilisation ---");
			return true;
		}

		// DANS LES ZONEBUILDING
		for (int i = 12; i < zones.length; i++)
		{
			// SI UNE ZONE EST VIDE
			if (((ZoneBuilding)zones[i]).isDeckEmpty() == true) {
				Printer.getPrinter().println("\n---- PARTIE TERMINEE : une des piles de batiment est vide ----");
				return true;
			}
		}
		return false;
	}


	/*==================================================
	 * Traitement choix de l'IA;
	 * ==================================================*/


	/**
	 * zoneChoose retourne la zone choisie par l'IA.
	 * @param player le joueur concerne
	 * @return l'indice de la zone choisie dans le tableau zones de Game.
	 */
	public int zoneChoose(Player player) {

		int choose = -1;
		boolean ok = false;
		//Variables utiles pour les fonctions de l'IA. 
		int[] zoneAvailableSpace = new int[zones.length];
		String[] zoneName = new String[zones.length];

		//Initialisation des variables pour l'IA
		for(int i = 0; i < zones.length; i++){
			zoneAvailableSpace[i] = zones[i].getAvailableSpace();
			zoneName[i] = zones[i].getName();
		}

		while(!ok){
			choose = player.getIA().chooseZone(zoneAvailableSpace,zoneName);

			if((choose >= 0) && (choose<zones.length) && ableToChooseZone(zones[choose], player)) ok=true;
			else Printer.getPrinter().println("/!\\ Zone "+zones[choose].getName()+" : Choix incorrecte ou zone pleine, veuillez reessayer./!\\");
		}
		return choose;
	}

	/**
	 * numberChoose retourne le nombre de figurines a posee choisie par l'IA.
	 * @param player le joueur concerne
	 * @param zone, la zone concernee
	 * @return Le nombre de figurines que l'IA veut poser. 
	 */
	public int numberChoose(Player player,Zone zone){
		int choose = -1;
		boolean ok = false;

		while(!ok){
			int max = Math.min(player.getCurrentFigurine(),zone.getAvailableSpace());
			int min = zone.getMinimalFigurineRequierement();

			//Pas besoin de demander a l'IA combien de figurines elle veut poser. 
			if(max == min) {
				return max; 
			}

			choose = player.getIA().chooseNumber(min,max);

			if(choose >= min && choose <= max) ok = true;
			else Printer.getPrinter().println("/!\\ Choix incorrecte, veuillez reessayer. /!\\");
		}
		return choose;
	}


	/**
	 * ableToPlaceFigurine() utilise les ableToPlaceFigurine() de zone et de player pour savoir si cela est possible. 
	 * @param zone : la zone concernee.
	 * @param player : le joueur concerne. 
	 * @return true si possible de placer, false sinon.
	 */ 
	private boolean ableToChooseZone(Zone zone,Player player){
		return zone.ableToChooseZone(player) && zone.getMinimalFigurineRequierement()<=player.getCurrentFigurine();
	}

}

