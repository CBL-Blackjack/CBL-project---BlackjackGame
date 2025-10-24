package bj.ui;

/**
 * Data model for the result card with theme-aware formatting.
 * Single source of truth for all card content.
 */
public class ResultCardModel {
    
    public enum Theme {
        WIN, LOSE, PUSH, BJ
    }
    
    private boolean visible = false;
    private String title = "";
    private String deltaFormatted = "";
    private String balanceFormatted = "";
    private Theme theme = Theme.PUSH;
    
    public ResultCardModel() {
        // Initialize with placeholder values
        title = "—";
        deltaFormatted = "—";
        balanceFormatted = "—";
    }
    
    // Getters
    public boolean isVisible() { return visible; }
    public String getTitle() { return title.isEmpty() ? "—" : title; }
    public String getDeltaFormatted() { return deltaFormatted.isEmpty() ? "—" : deltaFormatted; }
    public String getBalanceFormatted() { return balanceFormatted.isEmpty() ? "—" : balanceFormatted; }
    public Theme getTheme() { return theme; }
    
    // Setters
    public void setVisible(boolean visible) { this.visible = visible; }
    public void setTitle(String title) { this.title = title != null ? title : "—"; }
    public void setDeltaFormatted(String deltaFormatted) { this.deltaFormatted = deltaFormatted != null ? deltaFormatted : "—"; }
    public void setBalanceFormatted(String balanceFormatted) { this.balanceFormatted = balanceFormatted != null ? balanceFormatted : "—"; }
    public void setTheme(Theme theme) { this.theme = theme != null ? theme : Theme.PUSH; }
    
    /**
     * Set all card data at once
     */
    public void setCardData(String title, int netChange, int balance, Theme theme) {
        this.title = title != null ? title : "—";
        this.theme = theme != null ? theme : Theme.PUSH;
        
        // Format delta based on net change
        if (netChange > 0) {
            this.deltaFormatted = "+ $" + netChange;
        } else if (netChange < 0) {
            this.deltaFormatted = "– $" + Math.abs(netChange);
        } else {
            this.deltaFormatted = "$0";
        }
        
        // Format balance
        this.balanceFormatted = "Current Balance: $" + balance;
    }
    
    /**
     * Clear all data and hide card
     */
    public void clear() {
        visible = false;
        title = "—";
        deltaFormatted = "—";
        balanceFormatted = "—";
        theme = Theme.PUSH;
    }
}
