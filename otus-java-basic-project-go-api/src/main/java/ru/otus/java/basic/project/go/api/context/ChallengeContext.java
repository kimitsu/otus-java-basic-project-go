package ru.otus.java.basic.project.go.api.context;

/**
 * This context is created on clients and server when an outgoing challenge is created.
 * The challenged client will then specify this context id in the challenge response.
 * This context is also used to cancel the challenge.
 */
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
