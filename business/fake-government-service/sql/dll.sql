INSERT INTO address (administrative_area, street, street_number, apartment_number)
VALUES ('Kiev', 'Unknown str.', '1/2', '3');

INSERT INTO national_passport (first_name, last_name, father_name, date_of_birth, place_of_birth, image_path, sex,
                               issuer, date_of_issue)
VALUES ('John', 'Doe', 'John', '1970-01-01', 'Some place', 'test', 'male', 'government', '1970-01-01');

INSERT INTO place_of_residence (address_id, start_date, end_date, national_passport_id)
VALUES (1, '1970-01-01', '1970-01-01', 1);

INSERT INTO national_number (number, registration_date, issuer)
VALUES ('1234567890', '1970-01-01', 'government');

INSERT INTO known_identity (national_passport_id, national_number)
VALUES (1, '1234567890');