package eftm;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class WelcomeBot extends ListenerAdapter {
    
    private static final String TOKEN = "MTM0MTczMTc0MDQ5MzgwNzY5Ng.GvBvOi.zfWpnF7TZuRCgAidJeQfrO_-rxkydL_PQzA6nI";  // Replace with your bot's token
    private static final String WELCOME_CHANNEL_ID = "1128992111803502654";  // Replace with actual channel ID
    private static final String BACKGROUND_IMAGE_PATH = "C:\\Users\\conqu\\OneDrive\\Desktop\\EFootballTournamentManager\\eftm\\src\\main\\java\\eftm\\welcome-image.png"; // Replace with your background image

    public static void main(String[] args) {
        try {
            JDABuilder.createDefault(TOKEN)
                    .enableIntents(net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MEMBERS) // Required for user join events
                    .addEventListeners(new WelcomeBot())
                    .build();
            System.out.println("✅ WelcomeBot is running...");
        } catch (Exception e) {
            System.err.println("❌ Error starting the bot: " + e.getMessage());
        }
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        Member member = event.getMember();
        TextChannel welcomeChannel = event.getGuild().getTextChannelById(WELCOME_CHANNEL_ID);

        if (welcomeChannel == null) return;

        // Generate welcome image
        File welcomeImage = createWelcomeImage(member);

        // Send the welcome image
        if (welcomeImage != null) {
            welcomeChannel.sendMessage("🎉 Welcome " + member.getAsMention() + " to our server! 🎉")
                    .addFiles(net.dv8tion.jda.api.utils.FileUpload.fromData(welcomeImage))
                    .queue();
        } else {
            welcomeChannel.sendMessage("🎉 Welcome " + member.getAsMention() + " to our server! 🎉").queue();
        }
    }

    private File createWelcomeImage(Member member) {
        try {
            // Load background image
            BufferedImage background = ImageIO.read(new File(BACKGROUND_IMAGE_PATH));
            int bgWidth = background.getWidth();
            int bgHeight = background.getHeight();

            // Load user avatar
            URL avatarUrl = new URL(member.getUser().getEffectiveAvatarUrl() + "?size=256");
            BufferedImage avatar = ImageIO.read(avatarUrl);

            // Define profile pic size
            int profileSize = 90; // Adjust size if needed
            int centerX = (bgWidth - profileSize) / 2;
            int centerY = (bgHeight - profileSize) / 2 - 10; // Slightly above center

            // Resize and crop avatar into a circle
            avatar = makeCircularImage(resizeImage(avatar, profileSize, profileSize));

            // Draw everything on a new image
            Graphics2D g = background.createGraphics();
            g.drawImage(avatar, centerX, centerY, null);

            // Set text below avatar
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.setColor(Color.WHITE);

            // Calculate text width to center it
            String welcomeText = "Welcome, " + member.getUser().getName() + "!";
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(welcomeText);
            int textX = (bgWidth - textWidth) / 2;
            int textY = centerY + profileSize + 30; // Below the avatar

            g.drawString(welcomeText, textX, textY);
            g.dispose();

            // Save the image
            File outputFile = new File("welcome_output.png");
            ImageIO.write(background, "png", outputFile);
            return outputFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
        Image tmp = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return resizedImage;
    }

    private BufferedImage makeCircularImage(BufferedImage image) {
        int size = Math.min(image.getWidth(), image.getHeight());
        BufferedImage circularImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = circularImage.createGraphics();

        g2d.setClip(new Ellipse2D.Float(0, 0, size, size));
        g2d.drawImage(image, 0, 0, size, size, null);
        g2d.dispose();
        return circularImage;
    }
}
