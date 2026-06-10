package Day1.toDoApp.service;

import Day1.toDoApp.dao.TodoDAO;
import Day1.toDoApp.dao.TodoDAOImpl;
import Day1.toDoApp.model.Todo;

import java.util.List;

public class TodoService {
    private TodoDAOImpl todoDAO;

    public TodoService() {
        this.todoDAO = new TodoDAOImpl();
    }

    public void createTodo(int id, String task) {

        Todo todo = new Todo(id, task, false);

        todoDAO.addTodo(todo);
    }

    public void markCompleted(int id) {

        Todo todo = todoDAO.findById(id);

        if (todo != null) {
            todo.setCompleted(true);
            todoDAO.updateTodo(todo);
        }
    }

    public void removeTodo(int id) {

        todoDAO.deleteTodo(id);
    }

    public List<Todo> getAllTodos() {

        return todoDAO.findAll();
    }
}
