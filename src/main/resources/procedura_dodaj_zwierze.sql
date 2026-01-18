CREATE OR REPLACE PROCEDURE GenerujRaportSprzedazyBiletow(
    p_zoo_id NUMBER,
    p_miesiac NUMBER,
    p_rok NUMBER,
    p_podsumanie OUT SYS_REFCURSOR,
    p_szczegoly OUT SYS_REFCURSOR
) AS
BEGIN
    OPEN p_podsumanie FOR
    SELECT 
        z.Nazwa AS nazwa_zoo,
        p_miesiac || '/' || p_rok AS okres,
        NVL(SUM(b.Cena), 0) AS laczna_sprzedaz,
        COUNT(b.Bilet_ID) AS liczba_biletow,
        NVL(AVG(b.Cena), 0) AS srednia_cena
    FROM Zoo z
    LEFT JOIN Bilety b ON z.Zoo_ID = b.Zoo_ID
        AND EXTRACT(MONTH FROM b.Data) = p_miesiac
        AND EXTRACT(YEAR FROM b.Data) = p_rok
    WHERE z.Zoo_ID = p_zoo_id
    GROUP BY z.Nazwa;
    OPEN p_szczegoly FOR
    SELECT 
        TO_CHAR(b.Data, 'DD.MM.YYYY') AS data_biletu,
        b.Cena,
        b.Klient_ID
    FROM Bilety b
    WHERE b.Zoo_ID = p_zoo_id
        AND EXTRACT(MONTH FROM b.Data) = p_miesiac
        AND EXTRACT(YEAR FROM b.Data) = p_rok
    ORDER BY b.Data;
EXCEPTION
    WHEN OTHERS THEN
        OPEN p_podsumanie FOR SELECT NULL AS nazwa_zoo FROM DUAL WHERE 1=0;
        OPEN p_szczegoly FOR SELECT NULL AS data_biletu FROM DUAL WHERE 1=0;
        RAISE;
END GenerujRaportSprzedazyBiletow;
/