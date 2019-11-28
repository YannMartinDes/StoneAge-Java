package game;

import java.util.Arrays;

import inventory.Inventory;
import player.Player;
import printer.Printer;

public class Score {
	/**
	 * Calcule le score de chaque joueur (ne dois etre appeler que 1 fois et a la fin de la partie)
	 */
	public void calculScore(Player player,Inventory inventory) {

		CarteCivilisation[] cardCivi = inventory.getCardCivilisation().clone();
		int [] typeGreen = new int[8];
		int [] typeYellow = new int [4];
		int numberGreenCard = 0;
		
		for (int i =0 ; i < cardCivi.length;i++) {
			if (cardCivi[i].getTypeDownPart()<8) {
				typeGreen[cardCivi[i].getTypeDownPart()] += 1;
				numberGreenCard +=1;
			}
			else{
				typeYellow[cardCivi[i].getTypeDownPart()-typeGreen.length] += cardCivi[i].getNumberDownPart();
			}
		}

		int scoreAdd = 0;
		while (numberGreenCard > 0) {
			int numberDifferentCard = 0;
			
			for(int i = 0; i<typeGreen.length; i++) {
				if (typeGreen[i] > 0) {
					numberDifferentCard += 1;
					numberGreenCard -= 1;
					typeGreen[i] -= 1;
				}
			}
			Printer.getPrinter().println("Le joueur "+player.getName()+" obtient "
					+numberDifferentCard * numberDifferentCard+ " point(s) de victoire grace aux cartes a fond vert.");
			scoreAdd += numberDifferentCard * numberDifferentCard;
		}
		
		for(int i = 0; i<typeYellow.length; i++) {
			if (typeYellow[i] > 0) {
				if (i == 0) {
					scoreAdd += typeYellow[i]*inventory.getRessource(Ressource.FIELD);
					Printer.getPrinter().println("Le joueur "+player.getName()+" obtient "
							+typeYellow[i]*inventory.getRessource(Ressource.FIELD)+ " point(s) de victoire grace aux cartes paysans.");
				}
				if (i == 1) {
					scoreAdd += typeYellow[i]*inventory.getTools().getTool();
					Printer.getPrinter().println("Le joueur "+player.getName()+" obtient "
							+typeYellow[i]*inventory.getTools().getTool()+ " point(s) de victoire grace aux cartes fabricants d'outils.");
				}
				if (i == 2) {
					scoreAdd += typeYellow[i]*inventory.getBuildings();
					Printer.getPrinter().println("Le joueur "+player.getName()+" obtient "
							+typeYellow[i]*inventory.getBuildings()+ " point(s) de victoire grace aux cartes constructeur de maisons.");
				}	
				if (i == 3) {
					scoreAdd += typeYellow[i]*player.getMaxFigurine();
					Printer.getPrinter().println("Le joueur "+player.getName()+" obtient "
							+typeYellow[i]*player.getMaxFigurine()+ " point(s) de victoire grace aux cartes chamanes.");
				}
			}
		}
		scoreAdd += inventory.availableResourceToFeed();
		player.addScore(scoreAdd);
	}


	/**
	 * Affichage du classement
	 */
	public void classement(Player[] players) {
		int[] score = new int[players.length];
		for(int i = 0; i < players.length; i++) {
			score[i]=players[i].getScore();
		}
		Arrays.sort(score);
		Printer.getPrinter().println("\nVoici le classement:");
		int position =1;
		for(int i = players.length-1; i >= 0; i--) {
			for(Player player: players) {
				if(player.getScore() == score[i]) { 
					Printer.getPrinter().println("Posistion "+position+" : "+player.getName()+" avec un score de : "+player.getScore()); 
					break; //gerer les egalit� plus tard (le premier dans la liste est prioriter en cas d'egalit�)
				}
			}
			position++;
		}
	}
}
