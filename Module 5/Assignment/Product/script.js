let products = {
    "product1": {
        "name": "Product 1",
        "price": 10.99,
        "category": "Category 1",
        "description": "This is product 1."
    },
    "product2": {
        "name": "Product 2",
        "price": 19.99,
        "category": "Category 2",
        "description": "This is product 2."
    },
    "product3": {
        "name": "Product 3",
        "price": 5.99,
        "category": "Category 3",
        "description": "This is product 3."
    }
};

let tableBody = document.querySelector('#productsTable tbody');

function renderProducts(list) {
    tableBody.innerHTML = '';
    list.forEach(product => {
        let row = document.createElement('tr');
        row.className = 'cursor-pointer';
        row.addEventListener('click', () => {
            row.classList.toggle('bg-yellow-200');
        });

        let nameCell = document.createElement('td');
        nameCell.className = 'border border-gray-400 px-4 py-2';
        nameCell.textContent = product.name;
        row.appendChild(nameCell);

        let priceCell = document.createElement('td');
        priceCell.className = 'border border-gray-400 px-4 py-2';
        priceCell.textContent = product.price;
        row.appendChild(priceCell);

        let descriptionCell = document.createElement('td');
        descriptionCell.className = 'border border-gray-400 px-4 py-2';
        descriptionCell.textContent = product.description;
        row.appendChild(descriptionCell);

        let categoryCell = document.createElement('td');
        categoryCell.className = 'border border-gray-400 px-4 py-2';
        categoryCell.textContent = product.category;
        row.appendChild(categoryCell);

        tableBody.appendChild(row);
    });
}

// Initial render
renderProducts(Object.values(products));

let nameSortAsc = true;

function sortByName() {
    let sortedProducts = Object.values(products).sort((a, b) =>
        nameSortAsc
            ? a.name.localeCompare(b.name)
            : b.name.localeCompare(a.name)
    );

    nameSortAsc = !nameSortAsc; // flip direction for the next click
    renderProducts(sortedProducts);
}

let priceSortAsc = true;

function sortByPrice() {
    let sortedProducts = Object.values(products).sort((a,b) =>
        priceSortAsc
            ? (a.price > b.price ? 1 : -1) 
            : (b.price > a.price ? 1 : -1)
    );

    priceSortAsc = !priceSortAsc;
    renderProducts(sortedProducts);
}

function chooseCategory(event) {
    if (event) event.preventDefault();

    let filter = document.getElementById('categoryFilter');

    // Populate the dropdown with the available categories (only once).
    let categories = [...new Set(Object.values(products).map(p => p.category))];
    categories.forEach(category => {
        let option = document.createElement('option');
        option.value = category;
        option.textContent = category;
        filter.appendChild(option);
    });
}

// Populate the categories dropdown on load.
chooseCategory();

function filterByCategory(category) {
    let list = Object.values(products);

    if (category !== 'all') {
        list = list.filter(product => product.category === category);
    }

    renderProducts(list);
}