CREATE OR REPLACE PROCEDURE del_old_kall_logg_partitions
IS
    CURSOR kall_logg_partitions IS
        SELECT partition_name, high_value
        FROM USER_TAB_PARTITIONS
        WHERE TABLE_NAME = 'KALL_LOGG';
BEGIN
    FOR rec IN kall_logg_partitions
        LOOP
            IF to_date(substr(rec.high_value, 12, 10), 'YYYY-MM-DD') < CURRENT_DATE - 30 THEN
                EXECUTE IMMEDIATE 'ALTER TABLE kall_logg DROP PARTITION ' || rec.partition_name || ' UPDATE INDEXES';
            END IF;
        END LOOP;
END;
/
BEGIN
    DBMS_SCHEDULER.CREATE_JOB (
            job_name           =>  'delete_old_kall_logg_partitions',
            job_type           =>  'STORED_PROCEDURE',
            job_action         =>  'del_old_kall_logg_partitions',
            start_date         =>  TIMESTAMP '2022-10-01 05:00:00',
            repeat_interval    =>  'FREQ=DAILY;BYHOUR=05;BYMINUTE=00',
            enabled            =>  true);
END;
