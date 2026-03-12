import java.util.*;
import java.time.*;
import java.time.format.*;

// ============================================================
//  Smart Insight Expense Management System
//  Single-file Java console application
// ============================================================

public class SmartEMS {
    

    // ── Console colour codes ─────────────────────────────────
    static final String RESET  = "\u001B[0m";
    static final String CYAN   = "\u001B[36m";
    static final String GREEN  = "\u001B[32m";
    static final String YELLOW = "\u001B[33m";
    static final String RED    = "\u001B[31m";
    static final String BLUE   = "\u001B[34m";
    static final String PURPLE = "\u001B[35m";
    static final String WHITE  = "\u001B[37m";
    static final String DIM    = "\u001B[90m";

    // ════════════════════════════════════════════════════════
    //  Expense  — also serves as node in the doubly linked list
    // ════════════════════════════════════════════════════════
    static class Expense {
        int     id;
        String  title, category, date, note;
        double  amount;
        Expense next, prev;

        Expense(int id, String title, double amount,
                String category, String date, String note) {
            this.id       = id;
            this.title    = title;
            this.amount   = amount;
            this.category = category;
            this.date     = date;
            this.note     = note;
        }

        @Override
        public String toString() {
            return String.format("[#%-3d] %-22s | %-14s | %13s | %s",
                id, title, category, fmt(amount), date);
        }
    }

    // ════════════════════════════════════════════════════════
    //  User
    // ════════════════════════════════════════════════════════
    static class User {
        String name, email, password, type;

        User(String name, String email, String password, String type) {
            this.name = name; this.email = email;
            this.password = password; this.type = type;
        }
    }

    // ════════════════════════════════════════════════════════
    //  Activity Log — singly linked list, keeps last 10 entries
    // ════════════════════════════════════════════════════════
    static class ActivityLog {
        private static class Node {
            String data; Node next;
            Node(String d) { data = d; }
        }
        private Node head;
        private int  size;

        void addFirst(String data) {
            Node n = new Node(data);
            n.next = head; head = n; size++;
            if (size > 10) trimLast();
        }

        private void trimLast() {
            if (head == null) return;
            if (head.next == null) { head = null; size--; return; }
            Node cur = head;
            while (cur.next.next != null) cur = cur.next;
            cur.next = null; size--;
        }

        void display() {
            if (head == null) { say("  No activity yet.", DIM); return; }
            Node cur = head; int i = 1;
            while (cur != null) { say("  " + i++ + ". " + cur.data, WHITE); cur = cur.next; }
        }
    }

    // ════════════════════════════════════════════════════════
    //  Expense Store — doubly linked list
    // ════════════════════════════════════════════════════════
    static class ExpenseList {
        Expense head, tail;
        int     size;
        private int counter = 1;

        Expense add(String title, double amount,
                    String category, String date, String note) {
            Expense e = new Expense(counter++, title, amount, category, date, note);
            if (head == null) { head = tail = e; }
            else { e.prev = tail; tail.next = e; tail = e; }
            size++;
            return e;
        }

        boolean delete(int id) {
            Expense cur = head;
            while (cur != null) {
                if (cur.id == id) {
                    if (cur.prev != null) cur.prev.next = cur.next; else head = cur.next;
                    if (cur.next != null) cur.next.prev = cur.prev; else tail = cur.prev;
                    size--; return true;
                }
                cur = cur.next;
            }
            return false;
        }

        Expense[] toArray() {
            Expense[] a = new Expense[size]; Expense cur = head;
            for (int i = 0; i < size; i++) { a[i] = cur; cur = cur.next; }
            return a;
        }

        void displayForward() {
            if (head == null) { say("  No expenses recorded.", DIM); return; }
            for (Expense cur = head; cur != null; cur = cur.next) say("  " + cur, WHITE);
        }

        void displayBackward() {
            if (tail == null) { say("  No expenses recorded.", DIM); return; }
            for (Expense cur = tail; cur != null; cur = cur.prev) say("  " + cur, WHITE);
        }
    }

    // ════════════════════════════════════════════════════════
    //  Category List — circular linked list
    // ════════════════════════════════════════════════════════
    static class CategoryList {
        private static class Node {
            String data; Node next;
            Node(String d) { data = d; }
        }
        private Node tail;
        private int  size;

        void add(String data) {
            Node n = new Node(data);
            if (tail == null) { tail = n; tail.next = tail; }
            else { n.next = tail.next; tail.next = n; tail = n; }
            size++;
        }

        String[] toArray() {
            if (tail == null) return new String[0];
            String[] a = new String[size]; Node cur = tail.next;
            for (int i = 0; i < size; i++) { a[i] = cur.data; cur = cur.next; }
            return a;
        }

        void display() {
            String[] a = toArray();
            say("  " + String.join(", ", a), DIM);
        }
    }

    // ════════════════════════════════════════════════════════
    //  Generic Stack — backed by linked nodes
    // ════════════════════════════════════════════════════════
    static class Stack<T> {
        private static class Node<T> { T data; Node<T> next; Node(T d) { data = d; } }
        private Node<T> top; private int size;

        void push(T d) { Node<T> n = new Node<>(d); n.next = top; top = n; size++; }

        T pop() {
            if (isEmpty()) throw new NoSuchElementException("Stack underflow");
            T v = top.data; top = top.next; size--; return v;
        }

        T peek() {
            if (isEmpty()) throw new NoSuchElementException("Stack empty");
            return top.data;
        }

        boolean isEmpty() { return top == null; }
        int     size()    { return size; }
    }

    // ════════════════════════════════════════════════════════
    //  Generic Queue — backed by linked nodes
    // ════════════════════════════════════════════════════════
    static class Queue<T> {
        private static class Node<T> { T data; Node<T> next; Node(T d) { data = d; } }
        private Node<T> front, rear; private int size;

        void enqueue(T d) {
            Node<T> n = new Node<>(d);
            if (rear != null) rear.next = n;
            rear = n; if (front == null) front = n; size++;
        }

        T dequeue() {
            if (isEmpty()) throw new NoSuchElementException("Queue underflow");
            T v = front.data; front = front.next;
            if (front == null) rear = null; size--; return v;
        }

        boolean isEmpty() { return front == null; }
        int     size()    { return size; }
    }

    // ════════════════════════════════════════════════════════
    //  Circular Queue — fixed-size ring buffer for recent txns
    // ════════════════════════════════════════════════════════
    static class CircularQueue {
        private final String[] buf;
        private int front, rear, size;
        private final int cap;

        CircularQueue(int cap) {
            this.cap = cap; buf = new String[cap]; front = 0; rear = -1;
        }

        void enqueue(String d) {
            rear = (rear + 1) % cap;
            if (size == cap) front = (front + 1) % cap; else size++;
            buf[rear] = d;
        }

        void display() {
            if (size == 0) { say("  No recent transactions.", DIM); return; }
            for (int i = 0; i < size; i++)
                say("  " + (i + 1) + ". " + buf[(front + i) % cap], WHITE);
        }
    }

    // ════════════════════════════════════════════════════════
    //  Deque — double-ended queue for action history
    // ════════════════════════════════════════════════════════
    static class Deque<T> {
        private final LinkedList<T> list = new LinkedList<>();
        void    addFront(T d)  { list.addFirst(d); }
        void    addRear(T d)   { list.addLast(d); }
        T       removeFront()  { return list.removeFirst(); }
        T       removeRear()   { return list.removeLast(); }
        boolean isEmpty()      { return list.isEmpty(); }
        int     size()         { return list.size(); }

        void display() {
            if (list.isEmpty()) { say("  History is empty.", DIM); return; }
            int i = 1;
            for (T item : list) say("  " + i++ + ". " + item, WHITE);
        }
    }

    // ════════════════════════════════════════════════════════
    //  Hash Map — separate chaining; rehashes at 0.75 load
    // ════════════════════════════════════════════════════════
    static class HashMap<K, V> {
        private static class Entry<K, V> {
            K key; V value; Entry<K, V> next;
            Entry(K k, V v) { key = k; value = v; }
        }

        @SuppressWarnings("unchecked")
        private Entry<K, V>[] buckets = new Entry[16];
        private int cap = 16, size;

        private int hash(K key) {
            int h = key.hashCode(); h ^= (h >>> 16);
            return Math.abs(h % cap);
        }

        void put(K key, V value) {
            if ((double) size / cap >= 0.75) rehash();
            int idx = hash(key); Entry<K, V> cur = buckets[idx];
            while (cur != null) {
                if (cur.key.equals(key)) { cur.value = value; return; }
                cur = cur.next;
            }
            Entry<K, V> e = new Entry<>(key, value);
            e.next = buckets[idx]; buckets[idx] = e; size++;
        }

        V get(K key) {
            Entry<K, V> cur = buckets[hash(key)];
            while (cur != null) { if (cur.key.equals(key)) return cur.value; cur = cur.next; }
            return null;
        }

        boolean containsKey(K key) { return get(key) != null; }

        void remove(K key) {
            int idx = hash(key); Entry<K, V> cur = buckets[idx], prev = null;
            while (cur != null) {
                if (cur.key.equals(key)) {
                    if (prev == null) buckets[idx] = cur.next; else prev.next = cur.next;
                    size--; return;
                }
                prev = cur; cur = cur.next;
            }
        }

        @SuppressWarnings("unchecked")
        private void rehash() {
            Entry<K, V>[] old = buckets; int oldCap = cap;
            cap *= 2; buckets = new Entry[cap]; size = 0;
            for (int i = 0; i < oldCap; i++)
                for (Entry<K, V> cur = old[i]; cur != null; cur = cur.next)
                    put(cur.key, cur.value);
        }

        java.util.Set<K> keySet() {
            java.util.Set<K> keys = new java.util.LinkedHashSet<>();
            for (int i = 0; i < cap; i++)
                for (Entry<K, V> cur = buckets[i]; cur != null; cur = cur.next)
                    keys.add(cur.key);
            return keys;
        }

        int size() { return size; }
    }

    // ════════════════════════════════════════════════════════
    //  Min-Heap — priority queue used for insights
    // ════════════════════════════════════════════════════════
    static class InsightHeap {
        static class Node { int priority; String message; Node(int p, String m) { priority=p; message=m; } }
        private final Node[] arr; private int size;

        InsightHeap(int cap) { arr = new Node[cap]; }

        private int par(int i)  { return (i - 1) / 2; }
        private int left(int i) { return 2 * i + 1; }
        private int right(int i){ return 2 * i + 2; }

        void insert(int priority, String message) {
            if (size == arr.length) return;
            arr[size] = new Node(priority, message);
            int i = size++;
            while (i > 0 && arr[par(i)].priority > arr[i].priority) {
                Node t = arr[par(i)]; arr[par(i)] = arr[i]; arr[i] = t;
                i = par(i);
            }
        }

        Node extractMin() {
            if (size == 0) return null;
            Node min = arr[0]; arr[0] = arr[--size]; heapDown(0); return min;
        }

        private void heapDown(int i) {
            int s = i, l = left(i), r = right(i);
            if (l < size && arr[l].priority < arr[s].priority) s = l;
            if (r < size && arr[r].priority < arr[s].priority) s = r;
            if (s != i) { Node t = arr[i]; arr[i] = arr[s]; arr[s] = t; heapDown(s); }
        }

        boolean isEmpty() { return size == 0; }

        void displayAll() {
            if (size == 0) { say("  No insights available.", DIM); return; }
            InsightHeap copy = new InsightHeap(arr.length);
            for (int i = 0; i < size; i++) copy.insert(arr[i].priority, arr[i].message);
            int rank = 1;
            while (!copy.isEmpty()) {
                Node   n   = copy.extractMin();
                String tag = n.priority <= 1 ? RED    + "[URGENT]"
                           : n.priority <= 3 ? YELLOW + "[WARN]  "
                                             : GREEN  + "[INFO]  ";
                say("  " + rank++ + ". " + tag + RESET + "  " + n.message, WHITE);
            }
        }
    }

    // ════════════════════════════════════════════════════════
    //  Polynomial — used for spending trend projection
    // ════════════════════════════════════════════════════════
    static class Polynomial {
        private static class Term { double coeff; int exp; Term next; Term(double c,int e){coeff=c;exp=e;} }
        private Term head;

        void addTerm(double coeff, int exp) {
            if (coeff == 0) return;
            Term n = new Term(coeff, exp);
            if (head == null || head.exp < exp) { n.next = head; head = n; return; }
            Term cur = head;
            while (cur.next != null && cur.next.exp > exp) cur = cur.next;
            if (cur.exp == exp) { cur.coeff += coeff; return; }
            n.next = cur.next; cur.next = n;
        }

        double evaluate(double x) {
            double r = 0;
            for (Term cur = head; cur != null; cur = cur.next) r += cur.coeff * Math.pow(x, cur.exp);
            return r;
        }

        @Override
        public String toString() {
            if (head == null) return "0";
            StringBuilder sb = new StringBuilder();
            for (Term cur = head; cur != null; cur = cur.next) {
                if (sb.length() > 0 && cur.coeff > 0) sb.append(" + ");
                if (cur.exp == 0) sb.append(String.format("%.2f", cur.coeff));
                else              sb.append(String.format("%.2fx^%d", cur.coeff, cur.exp));
            }
            return sb.toString();
        }
    }

    // ════════════════════════════════════════════════════════
    //  Sorting
    // ════════════════════════════════════════════════════════
    static class Sorter {

        // Bubble Sort  O(n^2) — amount ascending
        static Expense[] bubble(Expense[] arr) {
            Expense[] a = arr.clone(); int n = a.length;
            for (int i = 0; i < n - 1; i++)
                for (int j = 0; j < n - i - 1; j++)
                    if (a[j].amount > a[j+1].amount) { Expense t=a[j]; a[j]=a[j+1]; a[j+1]=t; }
            return a;
        }

        // Insertion Sort  O(n^2) — date ascending
        static Expense[] insertion(Expense[] arr) {
            Expense[] a = arr.clone();
            for (int i = 1; i < a.length; i++) {
                Expense key = a[i]; int j = i - 1;
                while (j >= 0 && a[j].date.compareTo(key.date) > 0) { a[j+1]=a[j]; j--; }
                a[j+1] = key;
            }
            return a;
        }

        // Selection Sort  O(n^2) — category alphabetical
        static Expense[] selection(Expense[] arr) {
            Expense[] a = arr.clone();
            for (int i = 0; i < a.length - 1; i++) {
                int min = i;
                for (int j = i+1; j < a.length; j++)
                    if (a[j].category.compareTo(a[min].category) < 0) min = j;
                Expense t = a[i]; a[i] = a[min]; a[min] = t;
            }
            return a;
        }

        // Merge Sort  O(n log n) — amount descending
        static Expense[] merge(Expense[] arr) {
            if (arr.length <= 1) return arr;
            int mid = arr.length / 2;
            Expense[] l = merge(Arrays.copyOfRange(arr, 0, mid));
            Expense[] r = merge(Arrays.copyOfRange(arr, mid, arr.length));
            return doMerge(l, r);
        }

        private static Expense[] doMerge(Expense[] l, Expense[] r) {
            Expense[] res = new Expense[l.length + r.length];
            int i = 0, j = 0, k = 0;
            while (i < l.length && j < r.length)
                res[k++] = l[i].amount >= r[j].amount ? l[i++] : r[j++];
            while (i < l.length) res[k++] = l[i++];
            while (j < r.length) res[k++] = r[j++];
            return res;
        }

        // Quick Sort  O(n log n) average — title alphabetical
        static Expense[] quick(Expense[] arr) {
            Expense[] a = arr.clone(); qHelper(a, 0, a.length - 1); return a;
        }

        private static void qHelper(Expense[] a, int lo, int hi) {
            if (lo < hi) { int p = qPart(a, lo, hi); qHelper(a, lo, p-1); qHelper(a, p+1, hi); }
        }

        private static int qPart(Expense[] a, int lo, int hi) {
            String pivot = a[hi].title; int i = lo - 1;
            for (int j = lo; j < hi; j++)
                if (a[j].title.compareTo(pivot) <= 0) { i++; Expense t=a[i]; a[i]=a[j]; a[j]=t; }
            Expense t = a[i+1]; a[i+1] = a[hi]; a[hi] = t;
            return i + 1;
        }
    }

    // ════════════════════════════════════════════════════════
    //  Searching
    // ════════════════════════════════════════════════════════
    static class Searcher {

        // Linear Search  O(n) — title or category keyword
        static List<Expense> linear(Expense[] arr, String kw) {
            List<Expense> res = new ArrayList<>();
            String k = kw.toLowerCase();
            for (Expense e : arr)
                if (e.title.toLowerCase().contains(k) || e.category.toLowerCase().contains(k))
                    res.add(e);
            return res;
        }

        // Binary Search  O(log n) — by expense ID
        static Expense binary(Expense[] arr, int targetId) {
            Expense[] sorted = arr.clone();
            Arrays.sort(sorted, Comparator.comparingInt(e -> e.id));
            int lo = 0, hi = sorted.length - 1;
            while (lo <= hi) {
                int mid = lo + (hi - lo) / 2;
                if      (sorted[mid].id == targetId) return sorted[mid];
                else if (sorted[mid].id <  targetId) lo = mid + 1;
                else                                 hi = mid - 1;
            }
            return null;
        }
    }

    // ════════════════════════════════════════════════════════
    //  Expression Evaluator — uses Stack internally
    // ════════════════════════════════════════════════════════
    static class Expr {

        static boolean isBalanced(String s) {
            Stack<Character> st = new Stack<>();
            for (char c : s.toCharArray()) {
                if (c=='(' || c=='[' || c=='{') st.push(c);
                else if (c==')' || c==']' || c=='}') {
                    if (st.isEmpty()) return false;
                    char top = st.pop();
                    if ((c==')' && top!='(') || (c==']' && top!='[') || (c=='}' && top!='{'))
                        return false;
                }
            }
            return st.isEmpty();
        }

        private static int prec(char op) {
            return switch (op) { case '+','-' -> 1; case '*','/' -> 2; case '^' -> 3; default -> 0; };
        }

        static String toPostfix(String infix) {
            Stack<Character> st = new Stack<>();
            StringBuilder    sb = new StringBuilder();
            for (char c : infix.toCharArray()) {
                if (c == ' ') continue;
                if (Character.isDigit(c) || Character.isLetter(c)) { sb.append(c).append(' '); }
                else if (c == '(') st.push(c);
                else if (c == ')') {
                    while (!st.isEmpty() && st.peek() != '(') sb.append(st.pop()).append(' ');
                    if (!st.isEmpty()) st.pop();
                } else {
                    while (!st.isEmpty() && prec(st.peek()) >= prec(c)) sb.append(st.pop()).append(' ');
                    st.push(c);
                }
            }
            while (!st.isEmpty()) sb.append(st.pop()).append(' ');
            return sb.toString().trim();
        }

        static double evalPostfix(String postfix) {
            Stack<Double> st = new Stack<>();
            for (String tok : postfix.split("\\s+")) {
                if (tok.isEmpty()) continue;
                try { st.push(Double.parseDouble(tok)); }
                catch (NumberFormatException ex) {
                    double b = st.pop(), a = st.pop();
                    switch (tok.charAt(0)) {
                        case '+' -> st.push(a + b);
                        case '-' -> st.push(a - b);
                        case '*' -> st.push(a * b);
                        case '/' -> st.push(b != 0 ? a / b : 0.0);
                        case '^' -> st.push(Math.pow(a, b));
                    }
                }
            }
            return st.isEmpty() ? 0 : st.pop();
        }
    }

    // ════════════════════════════════════════════════════════
    //  Application state
    // ════════════════════════════════════════════════════════
    static final HashMap<String, User>   userStore     = new HashMap<>();
    static final HashMap<String, Double> budgetStore   = new HashMap<>();
    static final ExpenseList             expenses      = new ExpenseList();
    static final ActivityLog             activityLog   = new ActivityLog();
    static final CategoryList            categories    = new CategoryList();
    static final CircularQueue           recentTxns    = new CircularQueue(5);
    static final Deque<String>           actionHistory = new Deque<>();
    static final Queue<String>           notifications = new Queue<>();
    static final Stack<Expense>          undoStack     = new Stack<>();
    static       InsightHeap             insightHeap   = new InsightHeap(20);
    static       User                    currentUser   = null;
    static final Scanner                 sc            = new Scanner(System.in);

    // ════════════════════════════════════════════════════════
    //  Console helpers
    // ════════════════════════════════════════════════════════
    static String fmt(double v) { return String.format("Rs. %,.2f", v); }

    static void say(String text, String col) { System.out.println(col + text + RESET); }

    static void header(String title, String col) {
        String bar = "=".repeat(66);
        say("\n" + bar, col);
        int pad = Math.max(0, (66 - title.length()) / 2);
        say(" ".repeat(pad) + title, col);
        say(bar, col);
    }

    static void line() { say("-".repeat(66), BLUE); }

    static String ask(String prompt) { System.out.print(CYAN + prompt + RESET); return sc.nextLine().trim(); }

    static double askDouble(String prompt) {
        while (true) {
            try { return Double.parseDouble(ask(prompt)); }
            catch (NumberFormatException e) { say("  Please enter a valid number.", RED); }
        }
    }

    static int askInt(String prompt) {
        while (true) {
            try { return Integer.parseInt(ask(prompt)); }
            catch (NumberFormatException e) { say("  Please enter a valid number.", RED); }
        }
    }

    static String today() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    static void log(String msg)    { activityLog.addFirst("[" + today() + "] " + msg); actionHistory.addRear(msg); }
    static void alert(String msg)  { notifications.enqueue(msg); }

    // ════════════════════════════════════════════════════════
    //  Registration & Login
    // ════════════════════════════════════════════════════════
    static void register() {
        header("CREATE ACCOUNT", GREEN);

        String name = ask("  Full name      : ");
        if (name.isEmpty()) { say("  Name cannot be empty.", RED); return; }

        String email = ask("  Email          : ");
        if (!email.contains("@") || !email.contains(".")) { say("  Enter a valid email address.", RED); return; }
        if (userStore.containsKey(email)) { say("  This email is already registered.", RED); return; }

        String pass = ask("  Password       : ");
        if (pass.length() < 6) { say("  Password must be at least 6 characters.", RED); return; }

        say("  Account types:", YELLOW);
        say("  1. Student    2. Individual    3. Couple    4. Family", WHITE);
        String type = switch (askInt("  Choose type    : ")) {
            case 1 -> "Student"; case 2 -> "Individual";
            case 3 -> "Couple";  case 4 -> "Family";
            default -> "Individual";
        };

        userStore.put(email, new User(name, email, pass, type));
        say("\n  Account created. You can now log in.", GREEN);
        log("Registered: " + name);
        alert("Welcome, " + name + ". Your account has been created.");
    }

    static boolean login() {
        header("LOGIN", CYAN);
        String email = ask("  Email     : ");
        String pass  = ask("  Password  : ");
        User   u     = userStore.get(email);
        if (u == null || !u.password.equals(pass)) {
            say("  Incorrect email or password.", RED); return false;
        }
        currentUser = u;
        say("\n  Welcome back, " + u.name + "  [" + u.type + "]", GREEN);
        log("Login: " + u.name);
        alert("Logged in on " + today() + ".");
        return true;
    }

    // ════════════════════════════════════════════════════════
    //  Add / Delete / Undo
    // ════════════════════════════════════════════════════════
    static void addExpense() {
        header("ADD EXPENSE", GREEN);
        System.out.print(DIM + "  Categories: " + RESET);
        categories.display();

        String title = ask("  Title              : ");
        if (title.isEmpty()) { say("  Title cannot be empty.", RED); return; }

        double amount = askDouble("  Amount (Rs.)       : ");
        if (amount <= 0) { say("  Amount must be greater than zero.", RED); return; }

        String category = ask("  Category           : ");
        String date     = ask("  Date (yyyy-MM-dd)  : ");
        if (date.isEmpty()) date = today();
        String note = ask("  Note (optional)    : ");

        Expense e = expenses.add(title, amount, category, date, note);
        undoStack.push(e);
        recentTxns.enqueue(title + "  " + fmt(amount) + "  [" + date + "]");
        log("Added: " + title + " " + fmt(amount));
        alert("Expense added: " + title + " " + fmt(amount));
        say("\n  Expense #" + e.id + " saved.", GREEN);

        // Inline budget warning
        Double budget = budgetStore.get(category);
        if (budget != null) {
            double spent = 0;
            for (Expense ex : expenses.toArray()) if (ex.category.equals(category)) spent += ex.amount;
            double pct = (spent / budget) * 100;
            if      (pct > 100) say(String.format("  Warning: Over budget on %s (%.0f%% used).", category, pct), RED);
            else if (pct >  80) say(String.format("  Note: %.0f%% of your %s budget used.", pct, category), YELLOW);
        }
    }

    static void deleteExpense() {
        header("DELETE EXPENSE", RED);
        int id = askInt("  Expense ID to delete: ");
        if (expenses.delete(id)) { say("  Expense #" + id + " deleted.", GREEN); log("Deleted: #" + id); }
        else say("  No expense found with ID " + id + ".", RED);
    }

    static void undoAdd() {
        if (undoStack.isEmpty()) { say("  Nothing to undo.", DIM); return; }
        Expense e = undoStack.pop();
        expenses.delete(e.id);
        say("  Removed \"" + e.title + "\" [#" + e.id + "].", GREEN);
        log("Undo: removed #" + e.id);
    }

    // ════════════════════════════════════════════════════════
    //  View expenses
    // ════════════════════════════════════════════════════════
    static void viewExpenses() {
        header("ALL EXPENSES", CYAN);
        say("  ID     Title                   Category         Amount           Date", YELLOW);
        line();
        expenses.displayForward();
        line();
        double total = 0;
        for (Expense e : expenses.toArray()) total += e.amount;
        say("  Total: " + fmt(total) + "   Count: " + expenses.size, GREEN);
    }

    static void viewLatestFirst() {
        header("EXPENSES  -  LATEST FIRST", CYAN);
        say("  ID     Title                   Category         Amount           Date", YELLOW);
        line();
        expenses.displayBackward();
    }

    // ════════════════════════════════════════════════════════
    //  Sort
    // ════════════════════════════════════════════════════════
    static void sortExpenses() {
        header("SORT EXPENSES", PURPLE);
        say("  1.  Amount    (low to high)    Bubble Sort      O(n^2)", WHITE);
        say("  2.  Date      (old to new)     Insertion Sort   O(n^2)", WHITE);
        say("  3.  Category  (A to Z)         Selection Sort   O(n^2)", WHITE);
        say("  4.  Amount    (high to low)    Merge Sort       O(n log n)", WHITE);
        say("  5.  Title     (A to Z)         Quick Sort       O(n log n)", WHITE);
        int ch = askInt("  Choose: ");

        Expense[] arr = expenses.toArray();
        if (arr.length == 0) { say("  No expenses to sort.", DIM); return; }

        Expense[] sorted; String label;
        long start = System.nanoTime();
        switch (ch) {
            case 1 -> { sorted = Sorter.bubble(arr);    label = "Bubble Sort      O(n^2)"; }
            case 2 -> { sorted = Sorter.insertion(arr); label = "Insertion Sort   O(n^2)"; }
            case 3 -> { sorted = Sorter.selection(arr); label = "Selection Sort   O(n^2)"; }
            case 4 -> { sorted = Sorter.merge(arr);     label = "Merge Sort       O(n log n)"; }
            case 5 -> { sorted = Sorter.quick(arr);     label = "Quick Sort       O(n log n)"; }
            default -> { say("  Invalid choice.", RED); return; }
        }
        long ns = System.nanoTime() - start;

        say("\n  Algorithm : " + label, PURPLE);
        say(String.format("  Time      : %d ns  (%.4f ms)", ns, ns / 1_000_000.0), DIM);
        say("  Records   : " + arr.length, DIM);
        line();
        say("  ID     Title                   Category         Amount           Date", YELLOW);
        line();
        for (Expense e : sorted) say("  " + e, WHITE);
    }

    // ════════════════════════════════════════════════════════
    //  Search
    // ════════════════════════════════════════════════════════
    static void searchExpenses() {
        header("SEARCH", CYAN);
        say("  1. By keyword  (title or category)   Linear Search  O(n)", WHITE);
        say("  2. By ID                             Binary Search  O(log n)", WHITE);
        int ch = askInt("  Choose: ");

        Expense[] arr = expenses.toArray();
        if (arr.length == 0) { say("  No expenses to search.", DIM); return; }

        if (ch == 1) {
            String kw    = ask("  Keyword: ");
            long   start = System.nanoTime();
            List<Expense> res = Searcher.linear(arr, kw);
            long ns = System.nanoTime() - start;
            say(String.format("\n  Scanned %d records in %d ns", arr.length, ns), DIM);
            if (res.isEmpty()) say("  No matches found.", DIM);
            else { say("  Found " + res.size() + " result(s):", GREEN); for (Expense e : res) say("  " + e, WHITE); }

        } else if (ch == 2) {
            int     id    = askInt("  Expense ID: ");
            long    start = System.nanoTime();
            Expense found = Searcher.binary(arr, id);
            long    ns    = System.nanoTime() - start;
            int     steps = arr.length > 0 ? (int)(Math.log(arr.length) / Math.log(2)) + 1 : 0;
            say(String.format("\n  Max steps: %d  |  Time: %d ns", steps, ns), DIM);
            if (found == null) say("  Expense #" + id + " not found.", DIM);
            else say("  Found: " + found, GREEN);
        }
    }

    // ════════════════════════════════════════════════════════
    //  Budgets
    // ════════════════════════════════════════════════════════
    static void manageBudgets() {
        header("BUDGET MANAGER", YELLOW);
        say("  1. Set budget    2. View budgets    3. Remove budget", WHITE);
        int ch = askInt("  Choose: ");

        if (ch == 1) {
            System.out.print(DIM + "  Categories: " + RESET); categories.display();
            String cat = ask("  Category          : ");
            double amt = askDouble("  Monthly limit (Rs.): ");
            budgetStore.put(cat, amt);
            say("  Budget saved: " + cat + " = " + fmt(amt), GREEN);
            log("Budget set: " + cat + " = " + fmt(amt));

        } else if (ch == 2) {
            if (budgetStore.size() == 0) { say("  No budgets set.", DIM); return; }
            Expense[] all = expenses.toArray();
            line();
            double totalBudget = 0;
            for (String cat : budgetStore.keySet()) {
                double budget = budgetStore.get(cat), spent = 0;
                for (Expense e : all) if (e.category.equals(cat)) spent += e.amount;
                double pct    = budget > 0 ? (spent / budget) * 100 : 0;
                int    filled = (int) Math.min(pct / 5, 20);
                String bar    = "#".repeat(filled) + ".".repeat(20 - filled);
                String col    = pct > 100 ? RED : pct > 80 ? YELLOW : GREEN;
                say(String.format("  %-14s [%s] %5.1f%%   %s / %s",
                    cat, bar, pct, fmt(spent), fmt(budget)), col);
                totalBudget += budget;
            }
            line();
            say("  Total budgeted: " + fmt(totalBudget), GREEN);

        } else if (ch == 3) {
            String cat = ask("  Category to remove: ");
            budgetStore.remove(cat);
            say("  Budget for \"" + cat + "\" removed.", GREEN);
        }
    }

    // ════════════════════════════════════════════════════════
    //  Insights — built into a min-heap by priority
    // ════════════════════════════════════════════════════════
    static void buildInsights() {
        insightHeap = new InsightHeap(20);
        Expense[] all = expenses.toArray();
        if (all.length == 0) {
            insightHeap.insert(5, "No expenses recorded yet. Add some to get insights.");
            return;
        }
        java.util.Map<String, Double> totals = new java.util.HashMap<>();
        double total = 0;
        for (Expense e : all) { totals.merge(e.category, e.amount, Double::sum); total += e.amount; }

        for (String cat : budgetStore.keySet()) {
            double b = budgetStore.get(cat), s = totals.getOrDefault(cat, 0.0);
            double p = b > 0 ? (s / b) * 100 : 0;
            if      (p > 100) insightHeap.insert(1, "Over budget on " + cat + ". Spent " + fmt(s) + " of " + fmt(b) + ".");
            else if (p >  80) insightHeap.insert(2, String.format("%.0f%% of %s budget used. %.s remaining.", p, cat, fmt(b - s)));
        }

        for (var en : totals.entrySet()) {
            double p = total > 0 ? (en.getValue() / total) * 100 : 0;
            if (p > 40) insightHeap.insert(3, String.format("%s is %.0f%% of total spending (%s).", en.getKey(), p, fmt(en.getValue())));
        }

        if (budgetStore.size() == 0)
            insightHeap.insert(4, "No budgets set. Add category limits to monitor your spending.");

        if (all.length > 10)
            insightHeap.insert(4, all.length + " expense records found. Review and remove any duplicates.");

        String topCat = totals.entrySet().stream()
            .max(java.util.Map.Entry.comparingByValue())
            .map(java.util.Map.Entry::getKey).orElse("None");
        insightHeap.insert(5, "Highest spending: " + topCat + " (" + fmt(totals.getOrDefault(topCat, 0.0)) + ").");

        if (currentUser.type.equals("Student") && !totals.containsKey("Education"))
            insightHeap.insert(4, "Consider logging Education expenses to track your academic spending.");

        if (all.length >= 2) {
            double avg = total / all.length;
            Polynomial trend = new Polynomial();
            trend.addTerm(avg * 0.5, 2); trend.addTerm(avg, 1); trend.addTerm(50, 0);
            insightHeap.insert(5, "3-month projection: " + fmt(trend.evaluate(3))
                + "  (model: P(x) = " + trend + ")");
        }
    }

    static void viewInsights() {
        header("SMART INSIGHTS", GREEN);
        buildInsights();
        say("  Sorted by urgency\n", YELLOW);
        insightHeap.displayAll();
    }

    // ════════════════════════════════════════════════════════
    //  Expression Evaluator
    // ════════════════════════════════════════════════════════
    static void expressionMenu() {
        header("EXPRESSION EVALUATOR", PURPLE);
        say("  1. Check balanced symbols   e.g.  ({3+[2*1]})", WHITE);
        say("  2. Convert infix to postfix", WHITE);
        say("  3. Evaluate an expression", WHITE);
        int ch = askInt("  Choose: ");

        if (ch == 1) {
            String s = ask("  Expression: ");
            boolean ok = Expr.isBalanced(s);
            say("  " + (ok ? "Balanced." : "Not balanced."), ok ? GREEN : RED);

        } else if (ch == 2) {
            String infix   = ask("  Infix (use single-digit numbers): ");
            String postfix = Expr.toPostfix(infix);
            say("  Infix   : " + infix,   YELLOW);
            say("  Postfix : " + postfix,  GREEN);

        } else if (ch == 3) {
            String infix = ask("  Infix (use single-digit numbers): ");
            if (!Expr.isBalanced(infix)) { say("  Unbalanced symbols in expression.", RED); return; }
            String postfix = Expr.toPostfix(infix);
            double result  = Expr.evalPostfix(postfix);
            say("  Infix   : " + infix,          YELLOW);
            say("  Postfix : " + postfix,          YELLOW);
            say("  Result  : " + result,           GREEN);
        }
    }

    // ════════════════════════════════════════════════════════
    //  Shared account view (Couple / Family)
    // ════════════════════════════════════════════════════════
    static void viewShared() {
        header("SHARED ACCOUNT", CYAN);
        int members = currentUser.type.equals("Couple") ? 2 : 4;
        double total = 0;
        for (Expense e : expenses.toArray()) total += e.amount;
        say("  Type          : " + currentUser.type, WHITE);
        say("  Members       : " + members,           WHITE);
        say("  Total spent   : " + fmt(total),        GREEN);
        say("  Each person   : " + fmt(total / members), YELLOW);
        line();
        expenses.displayForward();
    }

    // ════════════════════════════════════════════════════════
    //  Profile
    // ════════════════════════════════════════════════════════
    static void viewProfile() {
        header("MY PROFILE", CYAN);
        Expense[] all = expenses.toArray();
        double total = 0; for (Expense e : all) total += e.amount;
        say("  Name       : " + currentUser.name,  WHITE);
        say("  Email      : " + currentUser.email, WHITE);
        say("  Type       : " + currentUser.type,  WHITE);
        say("  Expenses   : " + expenses.size,     WHITE);
        say("  Total      : " + fmt(total),         GREEN);
        say("  Average    : " + (all.length > 0 ? fmt(total / all.length) : "N/A"), WHITE);
        say("  Budgets    : " + budgetStore.size(), WHITE);
    }

    // ════════════════════════════════════════════════════════
    //  Tools submenu
    // ════════════════════════════════════════════════════════
    static void toolsMenu() {
        header("TOOLS", PURPLE);
        say("  1. Activity log", WHITE);
        say("  2. Recent transactions", WHITE);
        say("  3. Notifications", WHITE);
        say("  4. Action history", WHITE);
        say("  5. Spending trend projection", WHITE);
        say("  6. Expression evaluator", WHITE);
        int ch = askInt("  Choose: ");

        switch (ch) {
            case 1 -> { header("ACTIVITY LOG", CYAN); activityLog.display(); }
            case 2 -> { header("RECENT TRANSACTIONS", CYAN); recentTxns.display(); }
            case 3 -> {
                header("NOTIFICATIONS", CYAN);
                if (notifications.isEmpty()) { say("  No pending notifications.", DIM); break; }
                int n = notifications.size();
                for (int i = 0; i < n; i++) say("  - " + notifications.dequeue(), WHITE);
            }
            case 4 -> { header("ACTION HISTORY", CYAN); actionHistory.display(); }
            case 5 -> {
                header("SPENDING TREND", CYAN);
                Expense[] all = expenses.toArray();
                if (all.length == 0) { say("  Add expenses first.", DIM); break; }
                double total = 0; for (Expense e : all) total += e.amount;
                double avg = total / all.length;
                Polynomial p = new Polynomial();
                p.addTerm(avg * 0.5, 2); p.addTerm(avg, 1); p.addTerm(100, 0);
                say("  Trend model : P(x) = " + p, PURPLE);
                say("  1 month     : " + fmt(p.evaluate(1)), WHITE);
                say("  3 months    : " + fmt(p.evaluate(3)), WHITE);
                say("  6 months    : " + fmt(p.evaluate(6)), WHITE);
                say("  (x = months ahead)", DIM);
            }
            case 6 -> expressionMenu();
            default -> say("  Invalid choice.", RED);
        }
    }

    // ════════════════════════════════════════════════════════
    //  Main menu
    // ════════════════════════════════════════════════════════
    static void mainMenu() {
        while (true) {
            header("SMART EMS  -  " + currentUser.name + "  [" + currentUser.type + "]", CYAN);

            say("  EXPENSES", YELLOW);
            say("   1.  Add expense", WHITE);
            say("   2.  View all expenses", WHITE);
            say("   3.  View latest first", WHITE);
            say("   4.  Delete expense", WHITE);
            say("   5.  Undo last add", WHITE);
            say("   6.  Search expenses", WHITE);
            say("   7.  Sort expenses", WHITE);

            say("\n  ANALYTICS", YELLOW);
            say("   8.  Manage budgets", WHITE);
            say("   9.  Smart insights", WHITE);

            boolean shared = currentUser.type.equals("Couple") || currentUser.type.equals("Family");
            if (shared) {
                say("\n  SHARED", YELLOW);
                say("  10.  Shared account", WHITE);
            }

            say("\n  ACCOUNT", YELLOW);
            say("  11.  My profile", WHITE);
            say("  12.  Tools", WHITE);
            say("   0.  Logout", WHITE);
            line();

            int ch = askInt("  Choice: ");
            switch (ch) {
                case 1  -> addExpense();
                case 2  -> viewExpenses();
                case 3  -> viewLatestFirst();
                case 4  -> deleteExpense();
                case 5  -> undoAdd();
                case 6  -> searchExpenses();
                case 7  -> sortExpenses();
                case 8  -> manageBudgets();
                case 9  -> viewInsights();
                case 10 -> { if (shared) viewShared(); else say("  Not available for your account type.", RED); }
                case 11 -> viewProfile();
                case 12 -> toolsMenu();
                case 0  -> { say("\n  Goodbye, " + currentUser.name + ".", GREEN); currentUser = null; return; }
                default -> say("  Invalid choice. Please try again.", RED);
            }
        }
    }

    // ════════════════════════════════════════════════════════
    //  Entry point
    // ════════════════════════════════════════════════════════
    public static void main(String[] args) {
        for (String c : new String[]{"Food","Transport","Entertainment","Health","Shopping","Utilities","Education","Others"})
            categories.add(c);

        header("SMART INSIGHT EXPENSE MANAGEMENT SYSTEM", CYAN);
        say("  Track expenses, set budgets, and get smart financial insights.", DIM);

        while (true) {
            line();
            say("  1. Login    2. Register    0. Exit", WHITE);
            int ch = askInt("  Choice: ");
            if      (ch == 1) { if (login()) mainMenu(); }
            else if (ch == 2) register();
            else if (ch == 0) { say("\n  Goodbye.", CYAN); sc.close(); return; }
            else              say("  Invalid choice.", RED);
        }
    }
}