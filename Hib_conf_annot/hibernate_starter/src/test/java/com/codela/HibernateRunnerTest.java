package com.codela;

import com.codela.Entity.User;
import org.junit.jupiter.api.Test;

import javax.persistence.Column;
import javax.persistence.Table;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

class HibernateRunnerTest {
    @Test
    public void testHibernateApi() throws SQLException, IllegalAccessException {
        User user = User.builder().username("ivan1@name.com")
                .firstname("Ivan")
                .lastname("Ivanov")
                .birthDate(LocalDate.of(2000, 01, 01))
                .age(23).build();



        String tableName = Optional.of(user.getClass().getAnnotation(Table.class))
                .map(table -> table.schema() + "." + table.name())
                .orElse(user.getClass().getName());

        Field[] fields = user.getClass().getDeclaredFields();

        String columnNames = Arrays.stream(fields)
                .map(field -> Optional.ofNullable(field.getAnnotation(Column.class))
                        .map(Column::name)
                        .orElse(field.getName())
                ).collect(Collectors.joining(", "));

        String columnValues = Arrays.stream(fields)
                .map(field -> "?").collect(Collectors.joining(", "));
        Connection connection = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/thack", "codela", "1");

        String sql = String.format("insert into %s (%s) values (%s)", tableName, columnNames, columnValues);
        PreparedStatement preparedStatement = connection.prepareStatement(sql);

        for(int i = 0; i < fields.length; i++) {
            fields[i].setAccessible(true);
            preparedStatement.setObject(i + 1, fields[i].get(user));
        }
        preparedStatement.executeUpdate();
        preparedStatement.close();
        connection.close();
    }

}