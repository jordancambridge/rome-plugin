/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package romeplugin;

import blockchain.BalanceCommand;
import blockchain.BlockchainEventListener;
import blockchain.Ledger;
import blockchain.PayCommand;
import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;
import com.mysql.cj.jdbc.MysqlDataSource;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import romeplugin.database.SQLConn;
import romeplugin.newtitle.RemoveTitleCommand;
import romeplugin.newtitle.SetTitleCommand;
import romeplugin.newtitle.Title;
import romeplugin.newtitle.TitleEventListener;
import romeplugin.zoning.ClaimLandCommand;
import romeplugin.zoning.FoundCityCommand;
import romeplugin.zoning.LandControl;
import romeplugin.zoning.LandEventListener;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * @author chris
 */
public class RomePlugin extends JavaPlugin {
    public static final HashMap<Player, Title> onlinePlayerTitles = new HashMap<>();
    //Hashmap of players who joined the server and don't exist in the database
    //TODO: store these players when the server closes (and/or over a timed interval)
    public static final HashMap<Player, Title> toStore = new HashMap<>();
    //private final String titlesFilename = "rome_titles";
    // TODO: make the ledger persistent
    private final Ledger ledger = new Ledger();
    private final LandControl landControl = new LandControl(0, 0, 0, 5, 10);

    //private SQLConn connection;
    //runs when the plugin is enabled on the server startup 
    @Override
    public void onEnable() {
        //registering the eventlistener
        //try {
        //titles.loadData(new DataInputStream(new FileInputStream(titlesFilename)));
        //} catch (FileNotFoundException e) {
        //getLogger().fine("could not find " + titlesFilename);
        //}

        this.saveDefaultConfig();
        FileConfiguration config = this.getConfig();

        MysqlDataSource dataSource = new MysqlConnectionPoolDataSource();
        // we set our credentials
        dataSource.setServerName(config.getString("database.host"));
        dataSource.setPortNumber(config.getInt("database.port"));
        dataSource.setDatabaseName(config.getString("database.database"));
        dataSource.setUser(config.getString("database.username"));
        dataSource.setPassword(config.getString("database.password"));

        SQLConn.setSource(dataSource);

        try (Connection conn = SQLConn.getConnection()) {
            conn.prepareStatement("CREATE TABLE IF NOT EXISTS players (" +
                    "uuid CHAR(36) NOT NULL PRIMARY KEY," +
                    "title ENUM('TRIBUNE', 'SENATOR', 'MAYOR', 'JUDGE', 'CONSOLE', 'SENSOR', 'POPE', 'BUILDER', 'CITIZEN') NOT NULL);")
                    .execute();
            // (x0, y0) must be the top-left point and (x1, y1) must be the bottom-right point
            conn.prepareStatement("CREATE TABLE IF NOT EXISTS cityClaims (" +
                    "x0 INT NOT NULL," +
                    "y0 INT NOT NULL," +
                    "x1 INT NOT NULL," +
                    "y1 INT NOT NULL," +
                    "owner_uuid CHAR(36) NOT NULL);").execute();
            // overkill
            conn.prepareStatement("CREATE TABLE IF NOT EXISTS cityInfo (" +
                    "size INT NOT NULL," +
                    "x INT NOT NULL," +
                    "y INT NOT NULL);").execute();
            var res = conn.prepareStatement("SELECT * FROM cityInfo").executeQuery();
            if (res.next()) {
                res.previous();
                landControl.setGovernmentSize(res.getInt("size"));
                landControl.setCenter(res.getInt("x"), res.getInt("y"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        getCommand("claim").setExecutor(new ClaimLandCommand());
        getCommand("removetitle").setExecutor(new RemoveTitleCommand());
        getCommand("foundrome").setExecutor(new FoundCityCommand(landControl));
        getCommand("settitle").setExecutor(new SetTitleCommand());
        getCommand("pay").setExecutor(new PayCommand(ledger));
        getCommand("bal").setExecutor(new BalanceCommand(ledger));
        getServer().getPluginManager().registerEvents(new TitleEventListener(), this);
        getServer().getPluginManager().registerEvents(new BlockchainEventListener(this, ledger), this);
        getServer().getPluginManager().registerEvents(new LandEventListener(landControl), this);
    }

    //true/false if it worked or didnt work
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] arguments) {
        return false;
    }
}
