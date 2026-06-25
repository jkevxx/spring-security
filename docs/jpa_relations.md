# Comprehensive Guide to JPA (Jakarta Persistence API) Relationships

JPA relationships map the associations between database tables to object-oriented domain models in Java. Managing these relationships correctly is essential for application performance, data integrity, and clean code.

This guide details the two dimensions of JPA relationships: **Cardinality** (how many) and **Directionality** (who knows about whom).

---

## 1. Core Concepts: Directionality & Ownership

Every relationship in JPA has two dimensions:

### A. Directionality
*   **Unidirectional:** Only one entity contains a reference to the other.
    *   *Example:* `User` has a reference to `Address`, but `Address` has no knowledge of `User`.
*   **Bidirectional:** Both entities contain references to each other.
    *   *Example:* `User` has a reference to `Address`, and `Address` has a reference back to `User`.
    *   **Rule:** In a bidirectional relationship, you must define the **owning side** and the **inverse (non-owning) side**.

### B. Relationship Owner
The database itself has no concept of bidirectional relationships; it only knows about Foreign Keys (FK) defined in one of the tables.
*   **Owning Side:** The side that mapped directly to the Foreign Key column in the database. JPA looks at this side to determine when to insert or update the foreign key value.
*   **Inverse Side:** The side that references the owner. It is marked using the `mappedBy` attribute pointing to the property name in the owning entity. Changes made to the collection on the inverse side **do not** affect the database unless the owning side is updated too.

---

## 2. Relationship Types

### 2.1. One-to-One (`@OneToOne`)

A `@OneToOne` association maps a single entity instance to another single entity instance.

#### A. Unidirectional One-to-One
The owning side (`User`) has a foreign key to the target side (`Profile`). The `Profile` knows nothing about the `User`.

```java
@Entity
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String bio;
}

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_id", referencedColumnName = "id")
    private Profile profile;
}
```

*   **`@JoinColumn`**: Specifies the foreign key column in the owner's table (`user.profile_id`).

#### B. Bidirectional One-to-One
If you need to find the `User` given a `Profile`, you make it bidirectional. `Profile` is the inverse side, so it uses `mappedBy`.

```java
@Entity
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String bio;

    // Inverse side. "profile" is the field name inside the User class.
    @OneToOne(mappedBy = "profile")
    private User user;
}

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;

    // Owning side
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_id", referencedColumnName = "id")
    private Profile profile;
}
```

---

### 2.2. Many-to-One & One-to-Many (`@ManyToOne` / `@OneToMany`)

This is the most common relationship. In the database, the **Many** side always holds the Foreign Key.

#### A. Unidirectional Many-to-One
Many instances of `Order` belong to one `Customer`. `Order` is the owner because it holds the foreign key `customer_id`.

```java
@Entity
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
}

@Entity
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String orderNumber;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;
}
```

#### B. Bidirectional Many-to-One / One-to-Many
To access orders from a customer object, add a `@OneToMany` collection inside `Customer`.

```java
@Entity
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    // Inverse side. "customer" refers to the field name in the Order class.
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();

    // HELPER METHODS to keep bidirectional synchronization
    public void addOrder(Order order) {
        orders.add(order);
        order.setCustomer(this);
    }

    public void removeOrder(Order order) {
        orders.remove(order);
        order.setCustomer(null);
    }
}

@Entity
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String orderNumber;

    // Owning side (holds foreign key customer_id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;
}
```
> [!IMPORTANT]
> **Helper Methods:** Java objects do not automatically link when you set one side. You must manually manage both sides of a bidirectional relationship. Implement helper methods (like `addOrder` and `removeOrder` above) on the parent entity to ensure they stay in sync.

#### C. Unidirectional One-to-Many (Avoid if possible!)
If you only put `@OneToMany` in `Customer` without `@ManyToOne` in `Order`:

```java
@Entity
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany
    @JoinColumn(name = "customer_id") // Avoids a join table, but still has issues
    private List<Order> orders = new ArrayList<>();
}
```
> [!WARNING]
> Without `@ManyToOne` in `Order`, JPA is forced to create a join table (e.g., `customer_orders`) to track the relationship, unless you use `@JoinColumn`. Even with `@JoinColumn`, saving a new `Order` requires an `INSERT` statement first (leaving `customer_id` NULL) followed by an `UPDATE` statement to set `customer_id`.
> **Best Practice:** Prefer **Bidirectional One-to-Many** over Unidirectional One-to-Many.

---

### 2.3. Many-to-Many (`@ManyToMany`)

Represents a relationship where many records in one table relate to many records in another. A join table is required.

#### A. Unidirectional Many-to-Many
A `Student` can enroll in multiple `Course`s.

```java
@Entity
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
}

@Entity
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @ManyToMany
    @JoinTable(
        name = "student_course",
        joinColumns = @JoinColumn(name = "student_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private Set<Course> courses = new HashSet<>();
}
```

*   `@JoinTable`: Configures the mapping join table.
    *   `joinColumns`: The FK column pointing to the owning entity (`student_id`).
    *   `inverseJoinColumns`: The FK column pointing to the target entity (`course_id`).

#### B. Bidirectional Many-to-Many
If courses also need to reference students:

```java
@Entity
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;

    // Inverse side
    @ManyToMany(mappedBy = "courses")
    private Set<Student> students = new HashSet<>();
}

@Entity
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    // Owning side
    @ManyToMany
    @JoinTable(
        name = "student_course",
        joinColumns = @JoinColumn(name = "student_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private Set<Course> courses = new HashSet<>();
}
```

> [!CAUTION]
> **Use Set instead of List:** When adding or removing records in a bidirectional `@ManyToMany` relationship mapped as `List`, Hibernate will delete all rows from the join table and re-insert remaining rows. Using a `Set` ensures Hibernate only deletes/inserts the specific updated record.

#### C. The Real-World Alternative: Join Entity
In production, a join table often needs metadata (e.g., date of enrollment, grade). A plain `@ManyToMany` cannot hold extra columns on the join table.
Instead, split the `@ManyToMany` into two bidirectional `@ManyToOne` / `@OneToMany` relationships referencing a middle/junction entity.

```java
// 1. Junction Entity
@Entity
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    private LocalDateTime enrolledAt; // Extra column!
}

// 2. Student entity
@Entity
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "student")
    private List<Enrollment> enrollments = new ArrayList<>();
}

// 3. Course entity
@Entity
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "course")
    private List<Enrollment> enrollments = new ArrayList<>();
}
```

---

## 3. Best Practices & Pitfalls

### 3.1. Fetch Types (LAZY vs EAGER)
*   **`FetchType.EAGER`:** The target entity is fetched immediately along with the owner (often via SQL `JOIN`).
*   **`FetchType.LAZY`:** The target entity/collection is loaded dynamically only when it is accessed for the first time (via getter calls).

**Defaults in JPA:**
*   `@ManyToOne` & `@OneToOne`: **`EAGER`** (Default)
*   `@OneToMany` & `@ManyToMany`: **`LAZY`** (Default)

> [!TIP]
> **Performance Guideline:** Always override defaults to use **`FetchType.LAZY`** everywhere. Eager loading causes the infamous **N+1 query problem** and fetches unnecessary data from the database. Use entity graphs or `JOIN FETCH` queries when eager loading is actually needed.

### 3.2. Orphan Removal vs Cascade Delete
*   `cascade = CascadeType.REMOVE`: If a parent entity is deleted, all its related child entities are deleted.
*   `orphanRemoval = true`: If a child entity is removed from a parent's collection (e.g. `parent.getChildren().remove(child)`), that orphaned child is automatically deleted from the database.

### 3.3. Correct `equals()` and `hashCode()`
JPA entities are mutable, and their IDs are only populated *after* they are persisted to the database.
If you use default IDE-generated `equals` / `hashCode` methods (which use all fields including the database ID), you will experience bugs when placing entities inside a `Set` or caching them.

**Best Practice Implementation:**
Use a business key if available, or fall back to checking object identity and class type matching.

```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof User)) return false;
    User other = (User) o;
    return id != null && id.equals(other.getId());
}

@Override
public int hashCode() {
    return getClass().hashCode();
}
```
*Returning a constant hashCode is safe for JPA entities because it guarantees that the hashCode remains the same before and after the entity is saved to the database.*

---

## Summary Matrix

| Annotation | Owning Side | Target Side | Default Fetch | Recommended Fetch | Common Pitfalls |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **`@OneToOne`** | `@JoinColumn` | `mappedBy` | EAGER | LAZY | Eager loading defaults |
| **`@ManyToOne`** | `@JoinColumn` | - | EAGER | LAZY | N+1 Query Problem |
| **`@OneToMany`** | - | `mappedBy` | LAZY | LAZY | Unidirectional inserts/updates |
| **`@ManyToMany`** | `@JoinTable` | `mappedBy` | LAZY | LAZY | Collection duplicates, slow updates |
