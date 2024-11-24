package tfg.backend_tfg.model;

public class GitHubUserDetails {
    private String login;
    private int publicRepos;
    private int privateRepos;
    private int totalCommits;
    private int followers;
    private int following;
    private String company;
    private String location;

    // Getters y Setters
    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public int getPublicRepos() {
        return publicRepos;
    }

    public void setPublicRepos(int publicRepos) {
        this.publicRepos = publicRepos;
    }

    public int getPrivateRepos() {
        return privateRepos;
    }

    public void setPrivateRepos(int privateRepos) {
        this.privateRepos = privateRepos;
    }

    public int getTotalCommits() {
        return totalCommits;
    }

    public void setTotalCommits(int totalCommits) {
        this.totalCommits = totalCommits;
    }

    public int getFollowers() {
        return followers;
    }

    public void setFollowers(int followers) {
        this.followers = followers;
    }

    public int getFollowing() {
        return following;
    }

    public void setFollowing(int following) {
        this.following = following;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
