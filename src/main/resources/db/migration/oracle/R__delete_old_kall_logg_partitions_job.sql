BEGIN
    BEGIN
        DBMS_SCHEDULER.DROP_JOB(job_name => 'delete_old_kall_logg_partitions');
    EXCEPTION
        WHEN OTHERS THEN
            NULL; -- Ignore exception
    END;

    DBMS_SCHEDULER.CREATE_JOB(
            job_name           =>  'delete_old_kall_logg_partitions',
            job_type           =>  'STORED_PROCEDURE',
            job_action         =>  'del_old_kall_logg_partitions',
            start_date         =>  TIMESTAMP '2022-10-01 05:00:00',
            repeat_interval    =>  'FREQ=DAILY;BYHOUR=05;BYMINUTE=00',
            enabled            =>  true
    );
END;
