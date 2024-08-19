package ru.otus.java.basic.project.api.context;

public final class ChallengeContext extends Context {
    private String challenger = null;
    private String challenged = null;

    public ChallengeContext(Long id) {
        super(id);
    }

    public void setChallenger(String challenger) {
        this.challenger = challenger;
    }

    public String getChallenger() {
        return challenger;
    }

    public void setChallenged(String challenged) {
        this.challenged = challenged;
    }

    public String getChallenged() {
        return challenged;
    }
}
