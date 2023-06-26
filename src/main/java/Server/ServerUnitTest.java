package Server;

public class ServerUnitTest {
    public static void main(String[] args) {
        //here is written the driver code for starting the server
        //and also monitoring server events
        ServerRequestHandler requestHandler = new ServerRequestHandler();
        requestHandler.run();
    }
}

/*
INSERT INTO `Users` (`username`, `displayName`, `passwordHash`, `accessToken`, `email`, `dateOfBirth`, `accountMade`, `bio`, `location`, `countryId`, `phonenumber`)
VALUES
    ('john.doe', 'John Doe', '123456789', 'abc123xyz', 'johndoe123@example.com', '1990-01-01', NOW(), 'Hello, I am John!', 'New York City', 1, '123-456-7890'),
    ('jane.smith', 'Jane Smith', '234567890', 'def456xyz', 'janesmith456@example.com', '1985-03-12', NOW(), 'Hello, I am Jane!', 'San Francisco', 2, '234-567-8901'),
    ('bob.johnson', 'Bob Johnson', '345678901', 'ghi789xyz', 'bobjohnson789@example.com', '1978-07-05', NOW(), 'Hello, I am Bob!', 'Los Angeles', 3, '345-678-9012'),
    ('alice.brown', 'Alice Brown', '456789012', 'jkl012xyz', 'alicebrown012@example.com', '2000-02-29', NOW(), 'Hello, I am Alice!', 'Chicago', 4, '456-789-0123'),
    ('david.lee', 'David Lee', '567890123', 'mno123xyz', 'davidlee123@example.com', '1995-11-18', NOW(), 'Hello, I am David!', 'Houston', 5, '567-890-1234'),
    ('lisa.white', 'Lisa White', '678901234', 'pqr234xyz', 'lisawhite234@example.com', '1980-09-03', NOW(), 'Hello, I am Lisa!', 'Philadelphia', 6, '678-901-2345'),
    ('alex.kim', 'Alex Kim', '789012345', 'stu345xyz', 'alexkim345@example.com', '1998-05-21', NOW(), 'Hello, I am Alex!', 'Miami', 7, '789-012-3456'),
    ('mary.jones', 'Mary Jones', '890123456', 'vwx456xyz', 'maryjones456@example.com', '1992-12-25', NOW(), 'Hello, I am Mary!', 'Seattle', 8, '890-123-4567'),
    ('tom.wilson', 'Tom Wilson', '901234567', 'yza567xyz', 'tomwilson567@example.com', '1988-06-15', NOW(), 'Hello, I am Tom!', 'Dallas', 9, '901-234-5678'),
    ('samantha.green', 'Samantha Green', '012345678', 'bcd678xyz', 'samanthagreen678@example.com', '1997-04-09', NOW(), 'Hello, I am Samantha!', 'Washington, D.C.', 10, '012-345-6789');
*/