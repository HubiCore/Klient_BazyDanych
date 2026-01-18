CREATE OR REPLACE FUNCTION ZnajdzZwierzePoNazwie(
    p_nazwa_zwierzecia IN VARCHAR2
) RETURN VARCHAR2
AS
    v_wynik VARCHAR2(1000);
    v_nazwa_zwierzecia VARCHAR2(30);
    v_nazwa_gatunku VARCHAR2(30);
    v_nazwa_klatki VARCHAR2(30);
    v_nazwa_wybiegu VARCHAR2(40);
    v_nazwa_zoo VARCHAR2(30);
BEGIN
    SELECT 
        z.Nazwa,
        g.Nazwa,
        k.Nazwa,
        w.Nazwa,
        zoo.Nazwa
    INTO 
        v_nazwa_zwierzecia,
        v_nazwa_gatunku,
        v_nazwa_klatki,
        v_nazwa_wybiegu,
        v_nazwa_zoo
    FROM Zwierzeta z
    JOIN Gatunki g ON z.Gatunek_ID = g.Gatunek_ID
    JOIN Klatki k ON z.Klatka_ID = k.Klatka_ID
    JOIN Wybiegi w ON k.Wybieg_ID = w.Wybieg_ID
    JOIN Zoo zoo ON w.Zoo_ID = zoo.Zoo_ID
    WHERE UPPER(z.Nazwa) = UPPER(p_nazwa_zwierzecia)
      AND ROWNUM = 1;

    v_wynik := 'ZWIERZĘ: ' || v_nazwa_zwierzecia || 
               CHR(10) || 'Gatunek: ' || v_nazwa_gatunku ||
               CHR(10) || 'Klatka: ' || v_nazwa_klatki ||
               CHR(10) || 'Wybieg: ' || v_nazwa_wybiegu ||
               CHR(10) || 'Zoo: ' || v_nazwa_zoo;

    RETURN v_wynik;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RETURN 'Nie znaleziono zwierzęcia o nazwie: ' || p_nazwa_zwierzecia;
    WHEN OTHERS THEN
        RETURN 'Wystąpił błąd: ' || SQLERRM;
END ZnajdzZwierzePoNazwie;
/