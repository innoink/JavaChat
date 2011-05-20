/*
 * Copyright (C) 2011 Lugia Programming Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package Core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class Database
{
    private Connection conn;
    private Statement s;
    
    public Database(String connString)
    {
        System.out.printf("Initializing database connection.\n");
        System.out.printf("Connection String: %s\n", connString);
        try
        {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection(connString);
            s = conn.createStatement();
            System.out.printf("Database Connection Successful\n");
        }
        catch (Exception e)
        {
            System.out.printf("Database Connection Fail\n");
        }
    }
    
    public int execute(String sql, Object... args)
    {
        try
        {
            System.out.printf("SQL: " + sql + "\n", args);
            return s.executeUpdate(String.format(sql, args));
        }
        catch(Exception e)
        {
            return 0;
        }
    }
    
    public ResultSet query(String sql, Object... args)
    {
        try
        {
            System.out.printf("SQL: " + sql + "\n", args);
            return s.executeQuery(String.format(sql, args));
        }
        catch(Exception e)
        {
            return null;
        }
    }
}