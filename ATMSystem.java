import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.awt.event.*;
import javax.swing.Timer;
import java.util.concurrent.TimeUnit;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ATMSystem {
    private static final String FILE_NAME = "accounts.dat";
    private static ArrayList<Account> accounts = loadAccounts();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            showSplashScreen();
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            new LoginFrame();
        });
    }

    private static void showSplashScreen() {
        JWindow splash = new JWindow();
        JLabel splashLabel = new JLabel(
                "<html><center><font size='6' color='#0066cc'>ATM SYSTEM</font><br/><br/>Loading...</center></html>",
                JLabel.CENTER);
        splashLabel.setFont(new Font("Arial", Font.BOLD, 24));
        splash.getContentPane().setBackground(Color.WHITE);
        splash.getContentPane().add(splashLabel, BorderLayout.CENTER);
        splash.setSize(400, 300);
        splash.setLocationRelativeTo(null);
        splash.setVisible(true);

        Timer timer = new Timer(2000, e -> {
            splash.dispose();
        });
        timer.setRepeats(false);
        timer.start();
    }

    static class LoginFrame extends JFrame {
        JPasswordField pinField;
        JButton loginBtn, exitBtn, clearBtn, createBtn;
        int attempts = 0;
        Timer lockTimer;
        int[] lockTime = new int[1];

        LoginFrame() {
            setTitle("ATM Login");
            setSize(400, 300);
            setLocationRelativeTo(null);
            setLayout(new GridBagLayout());
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            getContentPane().setBackground(new Color(240, 240, 240));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            JLabel label = new JLabel("Enter 4-digit PIN:");
            label.setFont(new Font("Arial", Font.BOLD, 14));
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            add(label, gbc);

            pinField = new JPasswordField(4);
            pinField.setFont(new Font("Arial", Font.BOLD, 18));
            pinField.setHorizontalAlignment(JTextField.CENTER);
            pinField.setPreferredSize(new Dimension(100, 30));
            ((JPasswordField) pinField).setEchoChar('•');
            gbc.gridy = 1;
            add(pinField, gbc);

            pinField.addKeyListener(new KeyAdapter() {
                public void keyTyped(KeyEvent e) {
                    if (pinField.getPassword().length >= 4) {
                        e.consume();
                    }
                }
            });

            JPanel buttonPanel = new JPanel(new GridLayout(1, 4, 5, 0));
            loginBtn = createStyledButton("Login", new Color(0, 150, 0), KeyEvent.VK_L);
            clearBtn = createStyledButton("Clear", new Color(200, 120, 0), KeyEvent.VK_C);
            createBtn = createStyledButton("Create", new Color(0, 120, 200), KeyEvent.VK_A);
            exitBtn = createStyledButton("Exit", new Color(200, 0, 0), KeyEvent.VK_X);

            buttonPanel.add(loginBtn);
            buttonPanel.add(clearBtn);
            buttonPanel.add(createBtn);
            buttonPanel.add(exitBtn);
            gbc.gridy = 2;
            add(buttonPanel, gbc);

            pinField.addActionListener(e -> loginBtn.doClick());

            loginBtn.addActionListener(_e -> attemptLogin());
            clearBtn.addActionListener(_e -> {
                pinField.setText("");
                pinField.requestFocusInWindow();
            });
            exitBtn.addActionListener(_e -> confirmExit());
            createBtn.addActionListener(_e -> {
                new CreateAccountFrame();
                dispose();
            });

            setVisible(true);
            pinField.requestFocusInWindow();
        }

        private JButton createStyledButton(String text, Color bgColor, int mnemonic) {
            JButton button = new JButton(text);
            button.setBackground(bgColor);
            button.setForeground(Color.WHITE);
            button.setFont(new Font("Arial", Font.BOLD, 12));
            button.setMnemonic(mnemonic);
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            return button;
        }

        private void attemptLogin() {
            String pin = new String(pinField.getPassword()).trim();
            if (pin.length() != 4 || !pin.matches("\\d{4}")) {
                showShakeAnimation();
                JOptionPane.showMessageDialog(this,
                        "Please enter a valid 4-digit PIN",
                        "Invalid PIN", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Account acc = findAccountByPIN(pin);
            if (acc != null) {
                attempts = 0;
                new ATMFrame(acc);
                dispose();
            } else {
                attempts++;
                showShakeAnimation();
                if (attempts >= 3) {
                    lockAccountTemporarily();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Invalid PIN. Attempts left: " + (3 - attempts),
                            "Login Failed", JOptionPane.ERROR_MESSAGE);
                    pinField.setText("");
                }
            }
            pinField.requestFocusInWindow();
        }

        private void showShakeAnimation() {
            Point originalLocation = getLocation();
            int shakeDistance = 5;

            Timer shakeTimer = new Timer(30, null);
            shakeTimer.addActionListener(new ActionListener() {
                private int count = 0;
                private boolean direction = true;

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (count >= 10) {
                        setLocation(originalLocation);
                        shakeTimer.stop();
                        return;
                    }

                    int x = originalLocation.x + (direction ? shakeDistance : -shakeDistance);
                    setLocation(x, originalLocation.y);
                    direction = !direction;
                    count++;
                }
            });
            shakeTimer.start();
        }

        private void lockAccountTemporarily() {
            loginBtn.setEnabled(false);
            pinField.setEnabled(false);

            lockTime[0] = 30;
            JLabel lockMessage = new JLabel("System locked for " + lockTime[0] + " seconds");
            JOptionPane.showMessageDialog(this, lockMessage, "Account Locked", JOptionPane.ERROR_MESSAGE);

            lockTimer = new Timer(1000, e -> {
                lockTime[0]--;
                lockMessage.setText("System locked for " + lockTime[0] + " seconds");

                if (lockTime[0] <= 0) {
                    lockTimer.stop();
                    loginBtn.setEnabled(true);
                    pinField.setEnabled(true);
                    pinField.setText("");
                    pinField.requestFocusInWindow();
                    attempts = 0;
                    JOptionPane.showMessageDialog(this,
                            "You can now try to login again",
                            "Lock Expired", JOptionPane.INFORMATION_MESSAGE);
                }
            });
            lockTimer.start();
        }

        private void confirmExit() {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Exit ATM System?", "Confirm Exit",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        }
    }

    static class CreateAccountFrame extends JFrame {
        JTextField nameField, balanceField;
        JButton createBtn, backBtn, exitBtn;

        CreateAccountFrame() {
            setTitle("Create Account");
            setSize(500, 300);
            setLocationRelativeTo(null);
            setLayout(new GridBagLayout());
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            getContentPane().setBackground(new Color(240, 240, 240));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            JLabel nameLabel = new JLabel("Full Name:");
            nameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            gbc.gridx = 0;
            gbc.gridy = 0;
            add(nameLabel, gbc);

            nameField = new JTextField(20);
            nameField.setFont(new Font("Arial", Font.PLAIN, 14));
            gbc.gridx = 1;
            gbc.gridy = 0;
            add(nameField, gbc);

            JLabel balanceLabel = new JLabel("Initial Deposit (min 30 Birr):");
            balanceLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            gbc.gridx = 0;
            gbc.gridy = 1;
            add(balanceLabel, gbc);

            balanceField = new JTextField(20);
            balanceField.setFont(new Font("Arial", Font.PLAIN, 14));
            gbc.gridx = 1;
            gbc.gridy = 1;
            add(balanceField, gbc);

            createBtn = createStyledButton("Create", new Color(0, 150, 0), KeyEvent.VK_C);
            backBtn = createStyledButton("Back", new Color(0, 120, 200), KeyEvent.VK_B);
            exitBtn = createStyledButton("Exit", new Color(200, 0, 0), KeyEvent.VK_X);

            JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 5, 0));
            buttonPanel.add(createBtn);
            buttonPanel.add(backBtn);
            buttonPanel.add(exitBtn);
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.gridwidth = 2;
            add(buttonPanel, gbc);

            balanceField.addActionListener(e -> createBtn.doClick());
            nameField.addActionListener(e -> createBtn.doClick());

            createBtn.addActionListener(_e -> {
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Name cannot be empty");
                    nameField.requestFocusInWindow();
                    return;
                }
                if (!name.matches("^[A-Za-z\\s]+$")) {
                    JOptionPane.showMessageDialog(this, "Name must contain only letters and spaces");
                    nameField.requestFocusInWindow();
                    return;
                }
                try {
                    double balance = Double.parseDouble(balanceField.getText().trim());
                    if (balance < 30) {
                        JOptionPane.showMessageDialog(this, "Minimum initial deposit is 30 Birr");
                        balanceField.requestFocusInWindow();
                        return;
                    }

                    String accNum = generateAccountNumber();
                    String pin = generatePin();
                    Account acc = new Account(name, accNum, pin, balance);
                    accounts.add(acc);
                    saveAccounts();

                    JOptionPane.showMessageDialog(this,
                            "<html><div style='text-align: center;'>"
                                    + "<h2>Account Created!</h2>"
                                    + "<p>Account#: <b>1000" + accNum + "</b></p>"
                                    + "<p>PIN: <b>" + pin + "</b></p>"
                                    + "<p style='color: red;'>Please remember your PIN!</p>"
                                    + "</div></html>",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    new LoginFrame();
                    dispose();

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid number for balance");
                    balanceField.requestFocusInWindow();
                }
            });

            backBtn.addActionListener(_e -> {
                new LoginFrame();
                dispose();
            });

            exitBtn.addActionListener(_e -> {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Exit ATM System?", "Confirm Exit",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            });

            setVisible(true);
            nameField.requestFocusInWindow();
        }

        private JButton createStyledButton(String text, Color bgColor, int mnemonic) {
            JButton button = new JButton(text);
            button.setBackground(bgColor);
            button.setForeground(Color.WHITE);
            button.setFont(new Font("Arial", Font.BOLD, 12));
            button.setMnemonic(mnemonic);
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            return button;
        }
    }

    static class ATMFrame extends JFrame {
        JLabel welcomeLabel;
        JButton viewBalanceBtn, withdrawBtn, depositBtn, logoutBtn, transactionHistoryBtn, exitBtn;
        Account acc;

        ATMFrame(Account acc) {
            this.acc = acc;
            setTitle("ATM - " + acc.getName());
            setSize(500, 400);
            setLocationRelativeTo(null);
            setLayout(new GridBagLayout());
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            getContentPane().setBackground(new Color(240, 240, 240));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(15, 20, 15, 20);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;

            welcomeLabel = new JLabel("Welcome, " + acc.getName());
            welcomeLabel.setFont(new Font("Arial", Font.BOLD, 20));
            add(welcomeLabel, gbc);

            gbc.gridy = 1;
            viewBalanceBtn = createStyledButton("View Balance", new Color(0, 120, 200), KeyEvent.VK_V);
            add(viewBalanceBtn, gbc);

            gbc.gridy = 2;
            gbc.gridwidth = 1;
            withdrawBtn = createStyledButton("Withdraw", new Color(200, 0, 0), KeyEvent.VK_W);
            add(withdrawBtn, gbc);

            gbc.gridx = 1;
            depositBtn = createStyledButton("Deposit", new Color(0, 150, 0), KeyEvent.VK_D);
            add(depositBtn, gbc);

            gbc.gridy = 3;
            gbc.gridx = 0;
            transactionHistoryBtn = createStyledButton("Transactions", new Color(150, 0, 150), KeyEvent.VK_T);
            add(transactionHistoryBtn, gbc);

            gbc.gridx = 1;
            exitBtn = createStyledButton("Exit", new Color(200, 0, 0), KeyEvent.VK_X);
            add(exitBtn, gbc);

            gbc.gridy = 4;
            gbc.gridx = 0;
            gbc.gridwidth = 2;
            logoutBtn = createStyledButton("Logout", new Color(100, 100, 100), KeyEvent.VK_L);
            add(logoutBtn, gbc);

            viewBalanceBtn.addActionListener(_e -> showBalance());
            withdrawBtn.addActionListener(_e -> withdraw());
            depositBtn.addActionListener(_e -> deposit());
            transactionHistoryBtn.addActionListener(_e -> showTransactionHistory());
            exitBtn.addActionListener(_e -> confirmExit());
            logoutBtn.addActionListener(_e -> confirmLogout());

            setVisible(true);
        }

        private void showBalance() {
            JOptionPane.showMessageDialog(this,
                    "Current Balance: " + String.format("%.2f Birr", acc.getBalance()),
                    "Account Balance", JOptionPane.INFORMATION_MESSAGE);
        }

        private void withdraw() {
            String amtStr = JOptionPane.showInputDialog(this, "Enter amount to withdraw (Birr):");
            if (amtStr == null)
                return;
            try {
                double amt = Double.parseDouble(amtStr);
                if (amt <= 0) {
                    JOptionPane.showMessageDialog(this, "Enter a positive amount");
                    return;
                }
                if (acc.withdraw(amt)) {
                    String etDate = getEthiopianDate();
                    acc.addTransaction(String.format("[%s] Withdrawn: %.2f Birr", etDate, amt));
                    JOptionPane.showMessageDialog(this,
                            "<html>Withdrawal successful<br>Remaining balance: " +
                                    String.format("%.2f Birr", acc.getBalance()) + "</html>");
                    saveAccounts();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "<html>Insufficient balance<br>" +
                                    "Minimum 100 Birr must remain</html>");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number");
            }
        }

        private void deposit() {
            String amtStr = JOptionPane.showInputDialog(this, "Enter amount to deposit (Birr):");
            if (amtStr == null)
                return;
            try {
                double amt = Double.parseDouble(amtStr);
                if (amt <= 0) {
                    JOptionPane.showMessageDialog(this, "Enter a positive amount");
                    return;
                }
                acc.deposit(amt);
                String etDate = getEthiopianDate();
                acc.addTransaction(String.format("[%s] Deposited: %.2f Birr", etDate, amt));
                JOptionPane.showMessageDialog(this,
                        "<html>Deposit successful<br>New balance: " +
                                String.format("%.2f Birr", acc.getBalance()) + "</html>");
                saveAccounts();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number");
            }
        }

        private String getEthiopianDate() {
            LocalDate today = LocalDate.now();
            // Approximate Ethiopian Calendar conversion (subtract 7-8 years due to calendar
            // difference)
            long daysDifference = ChronoUnit.DAYS.between(LocalDate.of(1, 1, 1), today);
            LocalDate ethiopianApprox = LocalDate.of(1, 1, 1).plusDays(daysDifference - 2800); // Rough 7.6 years
            return String.format("%d/%d/%d", ethiopianApprox.getDayOfMonth(),
                    ethiopianApprox.getMonthValue(), ethiopianApprox.getYear());
        }

        private void showTransactionHistory() {
            List<String> transactions = acc.getTransactionHistory();
            if (transactions.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No transactions yet");
            } else {
                StringBuilder sb = new StringBuilder();
                // Show only the last 5 transactions (or all if less than 5)
                int startIndex = Math.max(0, transactions.size() - 5);
                for (int i = startIndex; i < transactions.size(); i++) {
                    sb.append(transactions.get(i)).append("\n");
                }
                JTextArea textArea = new JTextArea(sb.toString());
                textArea.setEditable(false);
                textArea.setFont(new Font("Arial", Font.PLAIN, 14));
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(350, 200));
                JOptionPane.showMessageDialog(this, scrollPane,
                        "Recent Transaction History (Last 5)", JOptionPane.INFORMATION_MESSAGE);
            }
        }

        private void confirmExit() {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Exit ATM System?", "Confirm Exit",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        }

        private void confirmLogout() {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Logout of account?", "Confirm Logout",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                new LoginFrame();
                dispose();
            }
        }

        private JButton createStyledButton(String text, Color bgColor, int mnemonic) {
            JButton button = new JButton(text);
            button.setBackground(bgColor);
            button.setForeground(Color.WHITE);
            button.setFont(new Font("Arial", Font.BOLD, 12));
            button.setMnemonic(mnemonic);
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
            return button;
        }
    }

    static String generateNumber() {
        Random rand = new Random();
        return String.format("%04d", rand.nextInt(10_000));
    }

    static String generatePin() {
        Random rand = new Random();
        String pin;
        do {
            pin = String.format("%04d", rand.nextInt(10_000));
        } while (findAccountByPIN(pin) != null);
        return pin;
    }

    static String generateAccountNumber() {
        Random rand = new Random();
        String num;
        do {
            num = String.format("%06d", rand.nextInt(1_000_000));
        } while (findAccountByNumber("1000" + num) != null);
        return num;
    }

    static Account findAccountByPIN(String pin) {
        for (Account a : accounts) {
            if (a.getPin().equals(pin))
                return a;
        }
        return null;
    }

    static Account findAccountByNumber(String accNum) {
        for (Account a : accounts) {
            if (a.getAccountNumber().equals(accNum))
                return a;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    static synchronized ArrayList<Account> loadAccounts() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            Object obj = ois.readObject();
            if (obj instanceof ArrayList) {
                return (ArrayList<Account>) obj;
            }
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Error loading accounts: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    static synchronized void saveAccounts() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(accounts);
        } catch (IOException e) {
            System.err.println("Error saving accounts: " + e.getMessage());
        }
    }
}

class Account implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final String accountNumber;
    private final String pin;
    private double balance;
    private List<String> transactionHistory;

    public Account(String name, String accountNumber, String pin, double balance) {
        this.name = name;
        this.accountNumber = accountNumber;
        this.pin = pin;
        this.balance = balance;
        this.transactionHistory = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public String getAccountNumber() {
        return "1000" + accountNumber;
    }

    public String getPin() {
        return pin;
    }

    public double getBalance() {
        return balance;
    }

    public List<String> getTransactionHistory() {
        return transactionHistory;
    }

    public void addTransaction(String transaction) {
        transactionHistory.add(transaction);
    }

    public boolean withdraw(double amount) {
        if (amount > 0 && balance - amount >= 100) {
            balance -= amount;
            return true;
        }
        return false;
    }

    public void deposit(double amount) {
        if (amount > 0)
            balance += amount;
    }
}