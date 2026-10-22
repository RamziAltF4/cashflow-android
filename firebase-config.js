/* ============================================================
   firebase-config.js — Firebase Init (Shared)
   ============================================================ */

const FIREBASE_CONFIG = {
    apiKey: "AIzaSyDZtkSIAsmFrQ8n2ALI1NKqCdne-ovRyb8",
    authDomain: "novaapp-b64ad.firebaseapp.com",
    projectId: "novaapp-b64ad",
    storageBucket: "novaapp-b64ad.firebasestorage.app",
    messagingSenderId: "807273905165",
    appId: "1:807273905165:web:492e6250ef4a280fe42944"
};

/* Load Firebase modular SDK via CDN */
(function loadFirebase() {
    const script = document.createElement('script');
    script.type = 'module';
    script.textContent = `
        import { initializeApp } from 'https://www.gstatic.com/firebasejs/10.12.0/firebase-app.js';
        import {
            getFirestore, doc, getDoc, setDoc, updateDoc, deleteDoc,
            collection, query, where, getDocs, addDoc, onSnapshot,
            serverTimestamp, orderBy, limit
        } from 'https://www.gstatic.com/firebasejs/10.12.0/firebase-firestore.js';
        import {
            getAuth, signInWithEmailAndPassword, createUserWithEmailAndPassword,
            signOut, onAuthStateChanged, updatePassword, signInWithCustomToken,
            RecaptchaVerifier, signInWithPhoneNumber
        } from 'https://www.gstatic.com/firebasejs/10.12.0/firebase-auth.js';

        const cfg = ${JSON.stringify(FIREBASE_CONFIG)};
        const app = initializeApp(cfg);

        const origGetDoc = getDoc;
        const wrappedGetDoc = (...args) => {
            let pathInfo = args[0]?.path || 'unknown';
            return origGetDoc(...args).catch(err => {
                console.error("FIREBASE REQUEST FAILED", { method: 'getDoc', code: err.code, message: err.message, path: pathInfo, auth: !!getAuth(app).currentUser, uid: getAuth(app).currentUser?.uid });
                throw err;
            });
        };

        const origGetDocs = getDocs;
        const wrappedGetDocs = (...args) => {
            let pathInfo = 'Query';
            try { if (args[0] && args[0].path) pathInfo = args[0].path; } catch(e){}
            return origGetDocs(...args).catch(err => {
                console.error("FIREBASE REQUEST FAILED", { method: 'getDocs', code: err.code, message: err.message, path: pathInfo, auth: !!getAuth(app).currentUser, uid: getAuth(app).currentUser?.uid });
                throw err;
            });
        };

        const origSetDoc = setDoc;
        const wrappedSetDoc = (...args) => {
            let pathInfo = args[0]?.path || 'unknown';
            return origSetDoc(...args).catch(err => {
                console.error("FIREBASE REQUEST FAILED", { method: 'setDoc', code: err.code, message: err.message, path: pathInfo, auth: !!getAuth(app).currentUser, uid: getAuth(app).currentUser?.uid });
                throw err;
            });
        };

        const origUpdateDoc = updateDoc;
        const wrappedUpdateDoc = (...args) => {
            let pathInfo = args[0]?.path || 'unknown';
            return origUpdateDoc(...args).catch(err => {
                console.error("FIREBASE REQUEST FAILED", { method: 'updateDoc', code: err.code, message: err.message, path: pathInfo, auth: !!getAuth(app).currentUser, uid: getAuth(app).currentUser?.uid });
                throw err;
            });
        };

        const origDeleteDoc = deleteDoc;
        const wrappedDeleteDoc = (...args) => {
            let pathInfo = args[0]?.path || 'unknown';
            return origDeleteDoc(...args).catch(err => {
                console.error("FIREBASE REQUEST FAILED", { method: 'deleteDoc', code: err.code, message: err.message, path: pathInfo, auth: !!getAuth(app).currentUser, uid: getAuth(app).currentUser?.uid });
                throw err;
            });
        };
        
        const origAddDoc = addDoc;
        const wrappedAddDoc = (...args) => {
            let pathInfo = args[0]?.path || 'unknown';
            return origAddDoc(...args).catch(err => {
                console.error("FIREBASE REQUEST FAILED", { method: 'addDoc', code: err.code, message: err.message, path: pathInfo, auth: !!getAuth(app).currentUser, uid: getAuth(app).currentUser?.uid });
                throw err;
            });
        };

        const origOnSnapshot = onSnapshot;
        const wrappedOnSnapshot = (...args) => {
            let pathInfo = args[0]?.path || 'Query';
            
            // Find error callback or add one
            let newArgs = [...args];
            let errorCbIndex = -1;
            
            if (typeof newArgs[1] === 'object') {
                // onSnapshot(query, options, next, error)
                if (typeof newArgs[3] === 'function') errorCbIndex = 3;
            } else if (typeof newArgs[1] === 'function' && typeof newArgs[2] === 'function') {
                // onSnapshot(query, next, error)
                errorCbIndex = 2;
            } else if (typeof newArgs[1] === 'object' && typeof newArgs[1].error === 'function') {
                // observer object
            }
            
            if (errorCbIndex !== -1) {
                const origErr = newArgs[errorCbIndex];
                newArgs[errorCbIndex] = (err) => {
                    console.error("FIREBASE REQUEST FAILED", { method: 'onSnapshot', code: err.code, message: err.message, path: pathInfo, auth: !!getAuth(app).currentUser, uid: getAuth(app).currentUser?.uid });
                    if(origErr) origErr(err);
                };
            } else if (typeof newArgs[1] === 'function' && newArgs.length === 2) {
                // No error callback provided, add one to catch unhandled
                newArgs.push((err) => {
                    console.error("FIREBASE REQUEST FAILED", { method: 'onSnapshot (unhandled)', code: err.code, message: err.message, path: pathInfo, auth: !!getAuth(app).currentUser, uid: getAuth(app).currentUser?.uid });
                });
            }
            
            try {
                return origOnSnapshot(...newArgs);
            } catch (err) {
                console.error("FIREBASE REQUEST FAILED (SYNC)", { method: 'onSnapshot', code: err.code, message: err.message, path: pathInfo, auth: !!getAuth(app).currentUser, uid: getAuth(app).currentUser?.uid });
                throw err;
            }
        };

        window.fb = {
            app,
            db: getFirestore(app),
            auth: getAuth(app),
            // Firestore
            doc, getDoc: wrappedGetDoc, setDoc: wrappedSetDoc, updateDoc: wrappedUpdateDoc, deleteDoc: wrappedDeleteDoc,
            collection, query, where, getDocs: wrappedGetDocs, addDoc: wrappedAddDoc, onSnapshot: wrappedOnSnapshot,
            serverTimestamp, orderBy, limit,
            // Auth
            signInWithEmailAndPassword, createUserWithEmailAndPassword,
            signOut, onAuthStateChanged, updatePassword, signInWithCustomToken,
            RecaptchaVerifier, signInWithPhoneNumber
        };

        window.dispatchEvent(new Event('firebase-ready'));
    `;
    document.head.appendChild(script);
})();

/* Helper: tunggu Firebase ready */
function firebaseReady() {
    return new Promise((resolve, reject) => {
        if (window.fb) return resolve(window.fb);
        window.addEventListener('firebase-ready', () => resolve(window.fb), { once: true });
        setTimeout(() => {
            if (!window.fb) reject(new Error('Firebase timeout — cek config / koneksi internet'));
        }, 10000);
    });
}