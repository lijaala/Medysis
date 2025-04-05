const CACHE_NAME = 'medysis-v1';
const urlsToCache = [
    '/',
    '/index.html', // If you have a default index.html in static
    '/css/style.css', // Assuming your main CSS file is here
    '/js/viewAppointment.js', // Assuming this is a key JS file
    '/js/addDiagnosis.js',
    '/js/dashboard.js',
    '/js/lab.js',
    '/js/labTechDashboard.js',
    '/js/labTest.js',
    '/js/patient.js',
    '/js/sidebar.js',
    '/js/staff.js',
    'js/viewPrescriptionAndLabResults.js',
    '/manifest.json',
    '/image/icon-192x192.png',
    '/image/icon-512x512.png',
    '/image',
    '/login',
    '/register',
    '/home',
    '/appointment',
    '/addPastmedical',
    '/forgotPassword',
    '/userHome',
    '/prescription',
    '/labView',
    '/settings',
    '/notification'
];

self.addEventListener('install', function(event) {
    // Perform install steps
    event.waitUntil(
        caches.open(CACHE_NAME)
            .then(function(cache) {
                console.log('Opened cache:', CACHE_NAME);
                return cache.addAll(urlsToCache);
            })
    );
});

self.addEventListener('fetch', function(event) {
    event.respondWith(
        caches.match(event.request)
            .then(function(response) {
                    // Cache hit - return response
                    if (response) {
                        console.log('Serving from cache:', event.request.url);
                        return response;
                    }
                    console.log('Fetching from network:', event.request.url);
                    return fetch(event.request);
                }
            )
    );
});

self.addEventListener('activate', function(event) {
    const cacheWhitelist = [CACHE_NAME];
    event.waitUntil(
        caches.keys().then(function(cacheNames) {
            return Promise.all(
                cacheNames.map(function(cacheName) {
                    if (cacheWhitelist.indexOf(cacheName) === -1) {
                        console.log('Deleting old cache:', cacheName);
                        return caches.delete(cacheName);
                    }
                })
            );
        })
    );
});