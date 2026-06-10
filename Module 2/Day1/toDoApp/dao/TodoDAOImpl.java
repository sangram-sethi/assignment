package Day1.toDoApp.dao;

import Day1.toDoApp.model.Todo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TodoDAOImpl implements TodoDAO {
    private List<Todo> todos = new ArrayList<>();

    @Override
    public void addTodo(Todo todo) {
        todos.add(todo);
    }

    @Override
    public Todo findById(int id) {

        for (Todo todo : todos) {
            if (todo.getId() == id) {
                return todo;
            }
        }

        return null;
    }

    @Override
    public List<Todo> findAll() {
        return todos;
    }

    @Override
    public void updateTodo(Todo updatedTodo) {

        for (Todo todo : todos) {

            if (todo.getId() == updatedTodo.getId()) {

                todo.setTask(updatedTodo.getTask());
                todo.setCompleted(updatedTodo.isCompleted());

                return;
            }
        }
    }

    @Override
    public void deleteTodo(int id) {

        Iterator<Todo> iterator = todos.iterator();

        while (iterator.hasNext()) {

            Todo todo = iterator.next();

            if (todo.getId() == id) {
                iterator.remove();
                return;
            }
        }
    }
}
