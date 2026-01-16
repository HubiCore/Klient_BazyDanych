-- First, drop existing tables if they exist (in reverse order due to dependencies)
DROP TABLE Karmienia CASCADE CONSTRAINTS;
DROP TABLE Zwierzeta CASCADE CONSTRAINTS;
DROP TABLE Gatunki CASCADE CONSTRAINTS;
DROP TABLE Klatki CASCADE CONSTRAINTS;
DROP TABLE Wybiegi CASCADE CONSTRAINTS;
DROP TABLE Pracownicy CASCADE CONSTRAINTS;
DROP TABLE Klienci CASCADE CONSTRAINTS;
DROP TABLE Bilet CASCADE CONSTRAINTS;
DROP TABLE Zoo CASCADE CONSTRAINTS;

-- Now create tables with corrected foreign key references
CREATE TABLE Zoo(
    Zoo_ID NUMBER(6) CONSTRAINT Zoo_PK PRIMARY KEY,
    Nazwa VARCHAR2(30)
);

CREATE TABLE Bilet(
    Bilet_ID NUMBER(6) CONSTRAINT Bilet_PK PRIMARY KEY,
    Data DATE,
    Cena NUMBER(6,2)
);

CREATE TABLE Pracownicy(
    Pracownik_ID NUMBER(6) CONSTRAINT Pracownik_PK PRIMARY KEY,
    Imie VARCHAR2(20),
    Nazwisko VARCHAR2(30),
    Wiek NUMBER(6),
    Pensja NUMBER(6,2),
    Miejsce_Pracy NUMBER(6) NOT NULL CONSTRAINT Zoo_Pracownicy_FK REFERENCES Zoo(Zoo_ID)
);

CREATE TABLE Klienci(
    Klient_ID NUMBER(6) CONSTRAINT Klient_PK PRIMARY KEY,
    Imie VARCHAR2(20),
    Wiek NUMBER(6),
    Zoo_odwiedzone NUMBER(6) CONSTRAINT Zoo_Klienci_FK REFERENCES Zoo(Zoo_ID),
    Numer_biletu NUMBER(6) CONSTRAINT Klienci_Bilet_FK REFERENCES Bilet(Bilet_ID)
);

CREATE TABLE Wybiegi(
    Wybieg_ID NUMBER(6) CONSTRAINT Wybieg_PK PRIMARY KEY,
    Nazwa VARCHAR2(40),
    Zoo_ID NUMBER(6) CONSTRAINT Wybiegi_Zoo_FK REFERENCES Zoo(Zoo_ID)
);

CREATE TABLE Klatki(
    Klatka_ID NUMBER(6) CONSTRAINT Klatka_PK PRIMARY KEY,
    Nazwa VARCHAR2(30),
    Wybieg_ID NUMBER(6) CONSTRAINT Klatka_Wybieg_FK REFERENCES Wybiegi(Wybieg_ID)
);

CREATE TABLE Gatunki(
    Gatunek_ID NUMBER(6) CONSTRAINT Gatunek_PK PRIMARY KEY,
    Nazwa VARCHAR2(30)
);

CREATE TABLE Zwierzeta(
    Zwierze_ID NUMBER(6) CONSTRAINT Zwierze_PK PRIMARY KEY,
    Nazwa VARCHAR2(30),
    Gatunek_ID NUMBER(6) NOT NULL CONSTRAINT Zwierze_Gatunek_FK REFERENCES Gatunki(Gatunek_ID),
    Klatka_ID NUMBER(6) CONSTRAINT Zwierze_Klatka_FK REFERENCES Klatki(Klatka_ID)
);

CREATE TABLE Karmienia(
    Karmienie_ID NUMBER(6) CONSTRAINT Karmienie_PK PRIMARY KEY,
    Data DATE,
    Ilosc NUMBER(6,2),
    Zwierze_ID NUMBER(6) CONSTRAINT Karmienie_Zwierze_FK REFERENCES Zwierzeta(Zwierze_ID)
);