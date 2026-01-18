CREATE OR REPLACE PROCEDURE DodajZwierze(
    p_nazwa_zwierzecia IN VARCHAR2,
    p_nazwa_gatunku IN VARCHAR2,
    p_nazwa_klatki IN VARCHAR2,
    p_id_opiekuna IN NUMBER,
    p_nowe_zwierze_id OUT NUMBER,
    p_komunikat OUT VARCHAR2
) AS
    v_gatunek_id NUMBER;
    v_klatka_id NUMBER;
    v_zwierze_id NUMBER;
    v_exists NUMBER;
    v_opiekun_id NUMBER;
BEGIN
    p_nowe_zwierze_id := NULL;
    p_komunikat := NULL;

    DBMS_OUTPUT.PUT_LINE('Rozpoczynam dodawanie zwierzęcia: ' || p_nazwa_zwierzecia);
    BEGIN
        SELECT COUNT(*) INTO v_exists 
        FROM Pracownicy 
        WHERE Pracownik_ID = p_id_opiekuna;
        
        IF v_exists = 0 THEN
            p_komunikat := 'Opiekun o podanym ID nie istnieje';
            RETURN;
        END IF;
    EXCEPTION
        WHEN OTHERS THEN
            p_komunikat := 'Błąd sprawdzania opiekuna: ' || SQLERRM;
            RETURN;
    END;
    DBMS_OUTPUT.PUT_LINE('Opiekun istnieje: ID=' || p_id_opiekuna);
    BEGIN
        SELECT Gatunek_ID INTO v_gatunek_id
        FROM Gatunki
        WHERE UPPER(Nazwa) = UPPER(p_nazwa_gatunku);
        DBMS_OUTPUT.PUT_LINE('Znaleziono gatunek: ID=' || v_gatunek_id);
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            SELECT NVL(MAX(Gatunek_ID), 0) + 1 INTO v_gatunek_id FROM Gatunki;
            DBMS_OUTPUT.PUT_LINE('Tworzę nowy gatunek: ID=' || v_gatunek_id);
            INSERT INTO Gatunki (Gatunek_ID, Nazwa)
            VALUES (v_gatunek_id, p_nazwa_gatunku);
            DBMS_OUTPUT.PUT_LINE('Nowy gatunek dodany');
    END;

    BEGIN
        SELECT Klatka_ID INTO v_klatka_id
        FROM Klatki
        WHERE UPPER(Nazwa) = UPPER(p_nazwa_klatki);
        DBMS_OUTPUT.PUT_LINE('Znaleziono klatkę: ID=' || v_klatka_id);
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            p_komunikat := 'Klatka o podanej nazwie nie istnieje';
            RETURN;
    END;

    BEGIN
        SELECT NVL(MAX(Zwierze_ID), 0) + 1 INTO v_zwierze_id FROM Zwierzeta;
        DBMS_OUTPUT.PUT_LINE('Nowe ID zwierzęcia: ' || v_zwierze_id);
        
        INSERT INTO Zwierzeta (Zwierze_ID, Nazwa, Gatunek_ID, Klatka_ID)
        VALUES (v_zwierze_id, p_nazwa_zwierzecia, v_gatunek_id, v_klatka_id);
        DBMS_OUTPUT.PUT_LINE('Zwierzę dodane do tabeli Zwierzeta');
    EXCEPTION
        WHEN OTHERS THEN
            p_komunikat := 'Błąd dodawania zwierzęcia: ' || SQLERRM;
            ROLLBACK;
            RETURN;
    END;

    BEGIN
        SELECT NVL(MAX(Opiekun_ID), 0) + 1 INTO v_opiekun_id FROM Opiekunowie;
        
        INSERT INTO Opiekunowie (Opiekun_ID, Pracownik_ID, Zwierze_ID)
        VALUES (v_opiekun_id, p_id_opiekuna, v_zwierze_id);
        
        DBMS_OUTPUT.PUT_LINE('Opiekun przypisany: Opiekun_ID=' || v_opiekun_id);
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Uwaga: Nie udało się przypisać opiekuna: ' || SQLERRM);
    END;

    COMMIT;
    
    -- Ustawienie parametrów OUT
    p_nowe_zwierze_id := v_zwierze_id;
    p_komunikat := 'Dodano zwierzę: ' || p_nazwa_zwierzecia || ' (ID: ' || v_zwierze_id || ')';
    
    DBMS_OUTPUT.PUT_LINE('Procedura zakończona sukcesem');

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_komunikat := 'Błąd ogólny: ' || SQLERRM;
        DBMS_OUTPUT.PUT_LINE('Błąd ogólny: ' || SQLERRM);
END DodajZwierze;
/