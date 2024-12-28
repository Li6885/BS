package entities;

import java.util.Objects;

public final class Session {
    private String username;

    // Constructor
    public Session(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "Session{" +
                "username='" + username + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Session session = (Session) o;
        return Objects.equals(username, session.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
