package cz.sspbrno.sql;

import cz.sspbrno.config;
import cz.sspbrno.html.ParseLoader;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class SQLConnect {
    private Connection con;
    private Statement st;

    private final String url = String.format("jdbc:mysql://localhost/%s?user=%s&password=%s",
            config.db_user, config.db_user, config.db_passwd);

    public SQLConnect() throws SQLException {
        this.con = DriverManager.getConnection(url);
        this.st = con.createStatement();
    }

    private void insert(ArrayList<String[]> result) throws SQLException {
        for (int i = 0; i < result.size(); i++) {
            String query = String.format("INSERT INTO demon (idDemon, Name, Creator) VALUES ('%s', '%s', '%s');",
                    result.get(i)[2], result.get(i)[0], result.get(i)[1]);
            st.executeUpdate(query);
        }

    }

    public void update() throws SQLException, IOException {
        st.executeUpdate("DELETE FROM demon");
        insert(new ParseLoader().splitElements());
    }

    public ArrayList<String[]> select(String table) throws SQLException {
        ResultSet rs = st.executeQuery("SELECT * FROM " + table);
        int col = rs.getMetaData().getColumnCount();
        ArrayList<String[]> oTable = new ArrayList<>();

        while (rs.next()) {
            String[] n = new String[col];
            for (int i = 0; i < col; i++) {
                n[i] = rs.getString(i+1);
            }
            oTable.add(n);
        }
        return oTable;
    }

    public void insertIntoCompletionist(String player, String device, String placement, String percentage) throws SQLException {
        String query = String.format("INSERT INTO completionist (Player, Device, Position, Percentage) VALUES ('%s', '%s', '%s', '%s');",
                player, device, placement, percentage);
        st.executeUpdate(query);
    }

    public void insertIntoCompletionist(String player, String device, String placement, String percentage, String proof) throws SQLException {
        String query = String.format("INSERT INTO completionist (Player, Device, Position, Percentage, Proof) VALUES ('%s', '%s', '%s', '%s', '%s');",
                player, device, placement, percentage, proof);
        st.executeUpdate(query);
    }

    public void updatePlayerName(String previous, String current) throws SQLException {
        String query = String.format("UPDATE completionist SET Player='%s' WHERE Player='%s';", current, previous);
        st.executeUpdate(query);
    }

    public void updateRecord(String player, String placement, String percentage) throws SQLException {
        String query = String.format("UPDATE completionist SET Percentage='%s' WHERE Player='%s' AND Position='%s';", percentage, player, placement);
        st.executeUpdate(query);
    }

    public void updateRecord(String player, String placement, String percentage, String proof) throws SQLException {
        String query = String.format("UPDATE completionist SET Percentage='%s' Proof='%s' WHERE Player='%s' AND Position='%s';", percentage, proof, player, placement);
        st.executeUpdate(query);
    }

    public void addProof(String player, String placement, String proof) throws SQLException {
        String query = String.format("INSERT INTO completionist (Proof) VALUES ('%s') WHERE Player='%s' AND Position='%s';",  proof, player, placement);
        st.executeUpdate(query);
    }

    public String getProof(String player, String placement) throws SQLException {
        String query = String.format("SELECT Proof FROM completionist WHERE Player='%s' AND Position='%s';", player, placement.toLowerCase());
        ResultSet rs = st.executeQuery(query);
        rs.next();
        return rs.getString(1);
    }

    public void removeRecord(String player, String placement) throws SQLException {
        String query = String.format("DELETE * FROM completionist WHERE Player='%s' AND Position='%s';", player, placement);
        st.executeUpdate(query);
    }

    public String listOfRecords(String player) throws SQLException {
        ArrayList<String[]> list = new ArrayList<>();
        ArrayList<String> alCompTemp = new ArrayList<>();
        ArrayList<String> alComp = new ArrayList<>();
        ArrayList<String> alDemo = new ArrayList<>();
        ResultSet rsComp = st.executeQuery("SELECT Player, Position, Percentage FROM completionist;");
        int colComp = rsComp.getMetaData().getColumnCount();
        while (rsComp.next()) {
            for (int i = 0; i < colComp; i++) {
                alCompTemp.add(rsComp.getString(i+1));
            }
        }
        ResultSet rsDemo = st.executeQuery("SELECT Name, idDemon FROM demon;");
        int colDemo = rsDemo.getMetaData().getColumnCount();
        while (rsDemo.next()) {
            for (int i = 0; i < colDemo; i++) {
                alDemo.add(rsDemo.getString(i+1));
            }
        }
        for (int i = 1; i <= alCompTemp.size()/3; i++) {
            for (int j = 1; j <= alCompTemp.get(i*3-3).split("\\+").length; j++) {
                if (alCompTemp.get(i*3-3).split("\\+")[j-1].equals(player)) {
                    String[] lol = {alCompTemp.get(i*3-3).split("\\+")[j-1], alCompTemp.get(i*3-2), alCompTemp.get(i*3-1)};
                    alComp.add(String.join(" ", lol));
                }
            }
        }
        for (int i = 0; i < alComp.size(); i++) {
            for (int j = 1; j <= alDemo.size()/2; j++) {
                String[] indRecord = {alComp.get(i).split(" ")[0], alComp.get(i).split(" ")[1], alComp.get(i).split(" ")[2], alDemo.get(j*2-1)};
                if (alComp.get(i).split(" ")[1].replace("-", " ").equals(alDemo.get(j*2-2).toLowerCase())) {
                    indRecord[1] = alDemo.get(j*2-2);
                    list.add(indRecord);
                }
            }
        }
        list.sort((o1, o2) -> {
            if (Integer.parseInt(o1[3]) < Integer.parseInt(o2[3])) return -1;
            else if (Integer.parseInt(o1[3]) > Integer.parseInt(o2[3])) return 1;
            return 0;
        });
        String[] arr = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = String.format("%s %s%%, ", list.get(i)[1], list.get(i)[2]);
        }
        return String.format("Best records for player: **%s** amount: %s\n%s", player, list.size(), String.join("", arr));
    }

    public String listOfPlayers() throws SQLException {
        List<String[]> list = new ArrayList<>();
        HashSet<String> hs = new HashSet<>();
        ArrayList<String> al = new ArrayList<>();
        ResultSet rs = st.executeQuery("SELECT Player FROM completionist;");
        int col = rs.getMetaData().getColumnCount();
        while (rs.next()) {
            for (int i = 0; i < col; i++) {
                al.add(rs.getString(i+1));
            }
        }
        for (int i = 0; i < al.size(); i++) {
            if (al.get(i).contains("+")) {
                al.addAll(Arrays.asList(al.get(i).split("\\+")));
            }
            hs.add(al.get(i));
        }
        for (int i = 0; i < hs.size(); i++) {
            int lol = 0;
            for (int j = 0; j < al.size(); j++) {
                if (al.get(j).equals(hs.toArray()[i])) lol++;
            }
            String[] arr = {String.valueOf(hs.toArray()[i]), String.valueOf(lol)};
            if (!String.valueOf(hs.toArray()[i]).contains("+")) list.add(arr);
        }
        list.sort((o1, o2) -> {
            if (Integer.parseInt(o1[1]) < Integer.parseInt(o2[1])) return 1;
            else if (Integer.parseInt(o1[1]) == Integer.parseInt(o2[1])) {
                if (o1[0].charAt(0) < o2[0].charAt(0)) return -1;
                else if (o1[0].charAt(0) == o2[0].charAt(0)) {
                    if (o1[0].charAt(1) < o2[0].charAt(1)) return -1;
                    else if (o1[0].charAt(1) == o2[0].charAt(1)) {
                        if (o1[0].charAt(2) < o2[0].charAt(2)) return -1;
                        else if (o1[0].charAt(2) > o2[0].charAt(2)) return 1;
                        else return 0;
                    }
                }
            }
            return -1;
        });
        String[] arr = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = list.get(i)[0];
        }
        return String.format("**All list players:**\n%s", String.join(", ", arr));
    }

    public void close() throws SQLException {
        con.close();
    }
}