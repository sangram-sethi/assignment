package Day1.toDoApp;

import Day1.toDoApp.model.Todo;
import Day1.toDoApp.service.TodoService;

public class Main {
    public static void main(String[] args) {
        TodoService service = new TodoService();

        service.createTodo(1, "Learn Java");
        service.createTodo(2, "Learn Collections");
        service.createTodo(3, "Learn DAO Pattern");

        System.out.println("All Todos:");

        for (Todo todo : service.getAllTodos()) {
            System.out.println(todo);
        }

        service.markCompleted(2);

        System.out.println("\nAfter completing Todo 2:");

        for (Todo todo : service.getAllTodos()) {
            System.out.println(todo);
        }

        service.removeTodo(1);

        System.out.println("\nAfter deleting Todo 1:");

         for (Todo todo : service.getAllTodos()) {
            System.out.println(todo);
        }
    }
}