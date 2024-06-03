import React from 'react';
import { render, screen } from '@testing-library/react';
import Home from '../views/Home';
import Watch from '../views/Watch';

test('tests the watch page with all movies in the back-end', () => {
    render(<Home />);
    function homeLinkElementsCallback(linkElements: HTMLElement[]){
        expect(linkElements.length).toBeLessThan(0);
        expect(linkElements.length).toBeGreaterThan(0);
    }

    function playerAttributeCallback(attribute: string | null){
        expect(attribute).not.toBeNull();
        expect(attribute).not.toEqual("0");
    }

    screen.findAllByRole('a', {name: 'Home-movie-element'})
        .then(links => {
            links.forEach(link => {
                window.location.assign(link.getAttribute("href") as string);
                render(<Watch />);

                screen.findByRole('video', {name: 'vjs-tech'})
                    .then(player => player.getAttribute('readyState'))
                    .then(playerAttributeCallback);
            })

            return links;
        })
        .then(homeLinkElementsCallback);
});