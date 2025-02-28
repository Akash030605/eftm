package eftm;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class TournamentBot extends ListenerAdapter {

    // Store tournaments and their teams
    private final Map<String, Tournament> tournaments = new HashMap<>();

    public static void main(String[] args) {
        // Replace "YOUR_BOT_TOKEN" with your bot's token
        JDA jda = JDABuilder.createDefault("MTM0MTczMTc0MDQ5MzgwNzY5Ng.GvBvOi.zfWpnF7TZuRCgAidJeQfrO_-rxkydL_PQzA6nI")
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new TournamentBot())
                .build();

        // Register slash commands
        jda.updateCommands().addCommands(
                Commands.slash("settournament", "Set the tournament name and banner")
                        .addOption(OptionType.STRING, "name", "The tournament name", true)
                        .addOption(OptionType.STRING, "banner", "The tournament banner URL", false),
                Commands.slash("addteam", "Add a team to the tournament")
                        .addOptions(
                                new OptionData(OptionType.STRING, "team", "The team name", true),
                                new OptionData(OptionType.STRING, "tournament", "The tournament name", true)
                                        .setAutoComplete(true)
                        ),
                Commands.slash("removeteam", "Remove a team from the tournament")
                        .addOptions(
                                new OptionData(OptionType.STRING, "team", "The team name", true),
                                new OptionData(OptionType.STRING, "tournament", "The tournament name", true)
                                        .setAutoComplete(true)
                        ),
                Commands.slash("recordmatch", "Record a match between two teams")
                        .addOptions(
                                new OptionData(OptionType.STRING, "team1", "The first team", true)
                                        .setAutoComplete(true),
                                new OptionData(OptionType.INTEGER, "score1", "The first team's score", true),
                                new OptionData(OptionType.STRING, "team2", "The second team", true)
                                        .setAutoComplete(true),
                                new OptionData(OptionType.INTEGER, "score2", "The second team's score", true)
                        ),
                Commands.slash("leaderboard", "Display the tournament leaderboard")
                        .addOptions(
                                new OptionData(OptionType.STRING, "tournament", "The tournament name", true)
                                        .setAutoComplete(true)
                        ),
                Commands.slash("deletetournament", "Delete a tournament")
                        .addOptions(
                                new OptionData(OptionType.STRING, "tournament", "The tournament name", true)
                                        .setAutoComplete(true)
                        ),
                Commands.slash("deletematch", "Delete a match result")
                        .addOptions(
                                new OptionData(OptionType.STRING, "tournament", "The tournament name", true)
                                        .setAutoComplete(true),
                                new OptionData(OptionType.STRING, "team1", "The first team", true)
                                        .setAutoComplete(true),
                                new OptionData(OptionType.STRING, "team2", "The second team", true)
                                        .setAutoComplete(true)
                        ),
                Commands.slash("mergetournaments", "Merge two tournaments")
                        .addOptions(
                                new OptionData(OptionType.STRING, "tournament1", "The first tournament", true)
                                        .setAutoComplete(true),
                                new OptionData(OptionType.STRING, "tournament2", "The second tournament", true)
                                        .setAutoComplete(true),
                                new OptionData(OptionType.STRING, "new_tournament_name", "The name of the new tournament", true)
                        )
        ).queue();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "settournament":
                handleSetTournamentCommand(event);
                break;
            case "addteam":
                handleAddTeamCommand(event);
                break;
            case "removeteam":
                handleRemoveTeamCommand(event);
                break;
            case "recordmatch":
                handleRecordMatchCommand(event);
                break;
            case "leaderboard":
                handleLeaderboardCommand(event);
                break;
            case "deletetournament":
                handleDeleteTournamentCommand(event);
                break;
            case "deletematch":
                handleDeleteMatchCommand(event);
                break;
            case "mergetournaments":
                handleMergeTournamentsCommand(event);
                break;
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        if (event.getName().equals("addteam") && event.getFocusedOption().getName().equals("tournament")) {
            // Provide autocomplete options for the tournament name
            List<Command.Choice> options = tournaments.keySet().stream()
                    .map(tournamentName -> new Command.Choice(tournamentName, tournamentName))
                    .collect(Collectors.toList());
            event.replyChoices(options).queue();
        } else if (event.getName().equals("recordmatch") && (event.getFocusedOption().getName().equals("team1") || event.getFocusedOption().getName().equals("team2"))) {
            // Provide autocomplete options for team names
            List<Command.Choice> options = tournaments.values().stream()
                    .flatMap(tournament -> tournament.getTeams().stream())
                    .map(teamName -> new Command.Choice(teamName, teamName))
                    .collect(Collectors.toList());
            event.replyChoices(options).queue();
        } else if (event.getName().equals("leaderboard") && event.getFocusedOption().getName().equals("tournament")) {
            // Provide autocomplete options for the tournament name in leaderboard
            List<Command.Choice> options = tournaments.keySet().stream()
                    .map(tournamentName -> new Command.Choice(tournamentName, tournamentName))
                    .collect(Collectors.toList());
            event.replyChoices(options).queue();
        } else if (event.getName().equals("deletetournament") && event.getFocusedOption().getName().equals("tournament")) {
            // Provide autocomplete options for the tournament name in deletetournament
            List<Command.Choice> options = tournaments.keySet().stream()
                    .map(tournamentName -> new Command.Choice(tournamentName, tournamentName))
                    .collect(Collectors.toList());
            event.replyChoices(options).queue();
        } else if (event.getName().equals("deletematch") && (event.getFocusedOption().getName().equals("tournament") || event.getFocusedOption().getName().equals("team1") || event.getFocusedOption().getName().equals("team2"))) {
            // Provide autocomplete options for tournament and team names in deletematch
            List<Command.Choice> options = tournaments.keySet().stream()
                    .map(tournamentName -> new Command.Choice(tournamentName, tournamentName))
                    .collect(Collectors.toList());
            event.replyChoices(options).queue();
        } else if (event.getName().equals("mergetournaments") && (event.getFocusedOption().getName().equals("tournament1") || event.getFocusedOption().getName().equals("tournament2"))) {
            // Provide autocomplete options for tournament names in mergetournaments
            List<Command.Choice> options = tournaments.keySet().stream()
                    .map(tournamentName -> new Command.Choice(tournamentName, tournamentName))
                    .collect(Collectors.toList());
            event.replyChoices(options).queue();
        }
    }

    private void handleSetTournamentCommand(SlashCommandInteractionEvent event) {
        String name = event.getOption("name").getAsString();
        String banner = event.getOption("banner") != null ? event.getOption("banner").getAsString() : null;

        tournaments.put(name, new Tournament(name, banner));
        event.reply(String.format("Tournament **%s** has been created!", name)).queue();
    }

    private void handleAddTeamCommand(SlashCommandInteractionEvent event) {
        String teamName = event.getOption("team").getAsString();
        String tournamentName = event.getOption("tournament").getAsString();

        if (!tournaments.containsKey(tournamentName)) {
            event.reply("Tournament **" + tournamentName + "** does not exist. Please create it first.").queue();
            return;
        }

        Tournament tournament = tournaments.get(tournamentName);
        tournament.addTeam(teamName);
        event.reply(String.format("Team **%s** has been added to tournament **%s**.", teamName, tournamentName)).queue();
    }

    private void handleRemoveTeamCommand(SlashCommandInteractionEvent event) {
        String teamName = event.getOption("team").getAsString();
        String tournamentName = event.getOption("tournament").getAsString();

        if (!tournaments.containsKey(tournamentName)) {
            event.reply("Tournament **" + tournamentName + "** does not exist.").queue();
            return;
        }

        Tournament tournament = tournaments.get(tournamentName);
        if (tournament.removeTeam(teamName)) {
            event.reply(String.format("Team **%s** has been removed from tournament **%s**.", teamName, tournamentName)).queue();
        } else {
            event.reply(String.format("Team **%s** does not exist in tournament **%s**.", teamName, tournamentName)).queue();
        }
    }

    private void handleRecordMatchCommand(SlashCommandInteractionEvent event) {
        String team1 = event.getOption("team1").getAsString();
        int score1 = event.getOption("score1").getAsInt();
        String team2 = event.getOption("team2").getAsString();
        int score2 = event.getOption("score2").getAsInt();

        // Find the tournament containing both teams
        Tournament tournament = tournaments.values().stream()
                .filter(t -> t.hasTeam(team1) && t.hasTeam(team2))
                .findFirst()
                .orElse(null);

        if (tournament == null) {
            event.reply("Both teams must belong to the same tournament. Please check the team names.").queue();
            return;
        }

        tournament.recordMatch(team1, score1, team2, score2);
        event.reply(String.format("Match recorded: **%s %d - %d %s** in tournament **%s**.", team1, score1, score2, team2, tournament.getName())).queue();
    }

    private void handleLeaderboardCommand(SlashCommandInteractionEvent event) {
        String tournamentName = event.getOption("tournament").getAsString();

        if (!tournaments.containsKey(tournamentName)) {
            event.reply("Tournament **" + tournamentName + "** does not exist.").queue();
            return;
        }

        Tournament tournament = tournaments.get(tournamentName);
        if (tournament.getTeams().isEmpty()) {
            event.reply("No teams have been added to the tournament yet.").queue();
            return;
        }

        // Create an embed for the leaderboard
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🏆 " + tournament.getName() + " Leaderboard 🏆")
                .setColor(new Color(0, 255, 0)) // Green color
                .setThumbnail(tournament.getBannerUrl() != null ? tournament.getBannerUrl() : "https://example.com/default-banner.png")
                .setDescription("Here are the current standings for the tournament:");

        // Add fields for each team in the leaderboard
        int rank = 1;
        for (Map.Entry<String, TeamStats> entry : tournament.getLeaderboard().entrySet()) {
            String team = entry.getKey();
            TeamStats stats = entry.getValue();

            embed.addField(
                    String.format("%d. %s", rank, team), // Rank and team name
                    String.format("```MP: %d | W: %d | T: %d | L: %d | GD: %d | Pts: %d```",
                            stats.getMatchesPlayed(), stats.getWins(), stats.getTies(), stats.getLosses(), stats.getGoalDifference(), stats.getPoints()),
                    false
            );
            rank++;
        }

        // Add a footer for extra flair
        embed.setFooter("Keep playing to climb the ranks!");

        // Send the embed to the channel
        event.replyEmbeds(embed.build()).queue();
    }

    private void handleDeleteTournamentCommand(SlashCommandInteractionEvent event) {
        String tournamentName = event.getOption("tournament").getAsString();

        if (!tournaments.containsKey(tournamentName)) {
            event.reply("Tournament **" + tournamentName + "** does not exist.").queue();
            return;
        }

        tournaments.remove(tournamentName);
        event.reply("Tournament **" + tournamentName + "** has been deleted.").queue();
    }

    private void handleDeleteMatchCommand(SlashCommandInteractionEvent event) {
        String tournamentName = event.getOption("tournament").getAsString();
        String team1 = event.getOption("team1").getAsString();
        String team2 = event.getOption("team2").getAsString();

        if (!tournaments.containsKey(tournamentName)) {
            event.reply("Tournament **" + tournamentName + "** does not exist.").queue();
            return;
        }

        Tournament tournament = tournaments.get(tournamentName);
        if (!tournament.hasTeam(team1) || !tournament.hasTeam(team2)) {
            event.reply("One or both teams do not exist in the tournament.").queue();
            return;
        }

        tournament.deleteMatch(team1, team2);
        event.reply("Match between **" + team1 + "** and **" + team2 + "** has been deleted.").queue();
    }

    private void handleMergeTournamentsCommand(SlashCommandInteractionEvent event) {
        String tournament1Name = event.getOption("tournament1").getAsString();
        String tournament2Name = event.getOption("tournament2").getAsString();
        String newTournamentName = event.getOption("new_tournament_name").getAsString();

        if (!tournaments.containsKey(tournament1Name) || !tournaments.containsKey(tournament2Name)) {
            event.reply("One or both tournaments do not exist.").queue();
            return;
        }

        Tournament tournament1 = tournaments.get(tournament1Name);
        Tournament tournament2 = tournaments.get(tournament2Name);

        // Check if both tournaments have the same teams
        if (!tournament1.getTeams().equals(tournament2.getTeams())) {
            event.reply("Cannot merge tournaments with different teams.").queue();
            return;
        }

        // Create a new tournament with the merged data
        Tournament newTournament = new Tournament(newTournamentName, null);
        newTournament.getTeams().addAll(tournament1.getTeams());

        // Merge match results
        tournament1.getTeamStats().forEach((team, stats) -> {
            newTournament.getTeamStats().put(team, new TeamStats(stats));
        });
        tournament2.getTeamStats().forEach((team, stats) -> {
            TeamStats newStats = newTournament.getTeamStats().get(team);
            newStats.matchesPlayed += stats.matchesPlayed;
            newStats.wins += stats.wins;
            newStats.ties += stats.ties;
            newStats.losses += stats.losses;
            newStats.goalsFor += stats.goalsFor;
            newStats.goalsAgainst += stats.goalsAgainst;
            newStats.points += stats.points;
        });

        tournaments.put(newTournamentName, newTournament);
        event.reply("Tournaments **" + tournament1Name + "** and **" + tournament2Name + "** have been merged into **" + newTournamentName + "**.").queue();
    }

    // Inner class to represent a tournament
    private static class Tournament {
        private final String name;
        private final String bannerUrl;
        private final Set<String> teams = new HashSet<>();
        private final Map<String, TeamStats> teamStats = new HashMap<>();

        public Tournament(String name, String bannerUrl) {
            this.name = name;
            this.bannerUrl = bannerUrl;
        }

        public String getName() {
            return name;
        }

        public String getBannerUrl() {
            return bannerUrl;
        }

        public void addTeam(String teamName) {
            teams.add(teamName);
            teamStats.put(teamName, new TeamStats());
        }

        public boolean removeTeam(String teamName) {
            teams.remove(teamName);
            return teamStats.remove(teamName) != null;
        }

        public boolean hasTeam(String teamName) {
            return teams.contains(teamName);
        }

        public Set<String> getTeams() {
            return teams;
        }

        public Map<String, TeamStats> getTeamStats() {
            return teamStats;
        }

        public void recordMatch(String team1, int score1, String team2, int score2) {
            TeamStats stats1 = teamStats.get(team1);
            TeamStats stats2 = teamStats.get(team2);

            stats1.matchesPlayed++;
            stats2.matchesPlayed++;

            stats1.goalsFor += score1;
            stats1.goalsAgainst += score2;
            stats2.goalsFor += score2;
            stats2.goalsAgainst += score1;

            if (score1 > score2) {
                stats1.wins++;
                stats2.losses++;
                stats1.points += 3;
            } else if (score1 < score2) {
                stats2.wins++;
                stats1.losses++;
                stats2.points += 3;
            } else {
                stats1.ties++;
                stats2.ties++;
                stats1.points += 1;
                stats2.points += 1;
            }
        }

        public void deleteMatch(String team1, String team2) {
            TeamStats stats1 = teamStats.get(team1);
            TeamStats stats2 = teamStats.get(team2);

            if (stats1 == null || stats2 == null) {
                return; // Teams do not exist
            }

            // Reverse the match result
            stats1.matchesPlayed--;
            stats2.matchesPlayed--;

            stats1.goalsFor -= stats1.goalsFor;
            stats1.goalsAgainst -= stats1.goalsAgainst;
            stats2.goalsFor -= stats2.goalsFor;
            stats2.goalsAgainst -= stats2.goalsAgainst;

            if (stats1.points > stats2.points) {
                stats1.wins--;
                stats2.losses--;
                stats1.points -= 3;
            } else if (stats1.points < stats2.points) {
                stats2.wins--;
                stats1.losses--;
                stats2.points -= 3;
            } else {
                stats1.ties--;
                stats2.ties--;
                stats1.points -= 1;
                stats2.points -= 1;
            }
        }

        public Map<String, TeamStats> getLeaderboard() {
            return teamStats.entrySet().stream()
                    .sorted((entry1, entry2) -> {
                        int pointsCompare = Integer.compare(entry2.getValue().getPoints(), entry1.getValue().getPoints());
                        if (pointsCompare != 0) return pointsCompare;
                        return Integer.compare(entry2.getValue().getGoalDifference(), entry1.getValue().getGoalDifference());
                    })
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        }
    }

    // Inner class to store team statistics
    private static class TeamStats {
        int matchesPlayed = 0;
        int wins = 0;
        int ties = 0;
        int losses = 0;
        int goalsFor = 0;
        int goalsAgainst = 0;
        int points = 0;
    
        // Default constructor
        public TeamStats() {}
    
        // Copy constructor
        public TeamStats(TeamStats other) {
            this.matchesPlayed = other.matchesPlayed;
            this.wins = other.wins;
            this.ties = other.ties;
            this.losses = other.losses;
            this.goalsFor = other.goalsFor;
            this.goalsAgainst = other.goalsAgainst;
            this.points = other.points;
        }
    
        // Getter methods
        public int getMatchesPlayed() {
            return matchesPlayed;
        }
    
        public int getWins() {
            return wins;
        }
    
        public int getTies() {
            return ties;
        }
    
        public int getLosses() {
            return losses;
        }
    
        public int getGoalsFor() {
            return goalsFor;
        }
    
        public int getGoalsAgainst() {
            return goalsAgainst;
        }
    
        public int getPoints() {
            return points;
        }
    
        public int getGoalDifference() {
            return goalsFor - goalsAgainst;
        }
    
        public int getRank() {
            // Rank is determined by sorting logic in the leaderboard
            return 0; // Placeholder, actual rank is calculated in the leaderboard
        }
    }
}