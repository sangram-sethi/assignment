package Day1.toDoApp.dao;

import Day1.toDoApp.model.Todo;

import java.util.List;

public interface TodoDAO {

    void addTodo(Todo todo);

    Todo findById(int id);

    List<Todo> findAll();

    void updateTodo(Todo todo);

    void deleteTodo(int id);
    
}
