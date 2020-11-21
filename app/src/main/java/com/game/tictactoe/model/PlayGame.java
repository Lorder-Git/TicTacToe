package com.game.tictactoe.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PlayGame {

    private String playerOneId;
    private String playerTwoId;
    private List<Integer> selectedCells;
    private boolean playerOneTurn;
    private String winnerId;
    private Date created;
    private String outOfGame;

    public PlayGame() {
    }

    public PlayGame(String playerOneId) {
        this.playerOneId = playerOneId;
        this.playerTwoId = "";
        this.selectedCells = new ArrayList<>();
        for (int i = 0; i < 9; i++){
            this.selectedCells.add(new Integer(0));
        }
        this.playerOneTurn = true;
        this.winnerId = "";
        this.created = new Date();
        this.outOfGame = "";
    }

    public String getPlayerOneId() {
        return playerOneId;
    }

    public void setPlayerOneId(String playerOneId) {
        this.playerOneId = playerOneId;
    }

    public String getPlayerTwoId() {
        return playerTwoId;
    }

    public void setPlayerTwoId(String playerTwoId) {
        this.playerTwoId = playerTwoId;
    }

    public List<Integer> getSelectedCells() {
        return selectedCells;
    }

    public void setSelectedCells(List<Integer> selectedCells) {
        this.selectedCells = selectedCells;
    }

    public boolean isPlayerOneTurn() {
        return playerOneTurn;
    }

    public void setPlayerOneTurn(boolean playerOneTurn) {
        this.playerOneTurn = playerOneTurn;
    }

    public String getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(String winnerId) {
        this.winnerId = winnerId;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getOutOfGame() {
        return outOfGame;
    }

    public void setOutOfGame(String outOfGame) {
        this.outOfGame = outOfGame;
    }
}
